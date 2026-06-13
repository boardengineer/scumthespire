package battleaimod.battleai.evolution;

import basemod.interfaces.PostUpdateSubscriber;
import battleaimod.battleai.evolution.utils.ValueFunctionManager;
import battleaimod.battleai.evolution.utils.fitness.AbstractFitness;
import battleaimod.battleai.evolution.utils.fitness.CompatExpression;
import battleaimod.battleai.evolution.utils.fitness.WeightedSumFitness;
import battleaimod.patches.FastActionPatches;
import battleaimod.utils.CommandAutomator;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import savestate.SaveState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FitnessFunctionEvaluator implements PostUpdateSubscriber {
    private boolean simRunning = false;
    private SaveState startingState;

    private boolean waitingForCombatToSave = false;
    private boolean waitingForCombatToBattle = false;
    private boolean waitingForDeckUpdate = false;
    private boolean waitingForCombatReset = false;

    private ArrayList<AbstractCard> startingDeck;
    private int startingHp;
    private int startingTurn;

    private AbstractFitness fitnessFunction;
    private FitnessType fitnessType;

    private final boolean ALLOW_FAST_MODE = true;
    public boolean combatFailed = false;
    private final double VERY_BAD_SCORE = -100000000;

    private static final String FITNESS_FILE = "CurrentFitnessValues.txt";
    private static final String OUTPUT_FILE = "FitnessEvaluationResults.txt";

    private enum FitnessType {
        WEIGHTED_SUM,
        EXPRESSION_TREE
    }

    @Override
    public void receivePostUpdate() {
        if (!simRunning && Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            System.out.println("starting fitness function evaluation");
            simRunning = true;
            initEvaluation();
            return;
        }

        if(!simRunning) return;

        if (waitingForDeckUpdate) {
            if (checkDeckUpdated()) {
                toggleFast(true);
                startingHp = AbstractDungeon.player.currentHealth;
                waitingForDeckUpdate = false;
                CommandAutomator.restartCurrentFight();
                waitingForCombatToSave = true;
            }
            return;
        }

        if (waitingForCombatToSave) {
            if (isInCombat()) {
                startingState = new SaveState();
                startingTurn = GameActionManager.turn;
                waitingForCombatToSave = false;
                startNewCombat();
            }
            return;
        }

        if (waitingForCombatReset) {
            if (AbstractDungeon.player.currentHealth == startingHp && GameActionManager.turn == startingTurn) {
                waitingForCombatReset = false;
                startNextCombat();
            }
            return;
        }

        if (waitingForCombatToBattle) {
            if (isInCombat()) {
                EvolutionManager.canRunAutoBattler = true;
                waitingForCombatToBattle = false;
            }
            return;
        }

        if (EvolutionManager.canRunAutoBattler) {
            if (combatOver()) {
                EvolutionManager.canRunAutoBattler = false;
                System.out.println("combat over detected!!");

                writeCombatResult();

                if (combatFailed) {
                    AbstractDungeon.overlayMenu.endTurnButton.disable(true);
                }

                startNewCombat();
            }
        }
    }

    public void failCombat() {
        combatFailed = true;
        AbstractDungeon.actionManager.addToTop(new LoseHPAction(AbstractDungeon.player, AbstractDungeon.player, 999));
    }

    private boolean combatOver() {
        return isDead() || (isInCombat() && AbstractDungeon.getCurrRoom().isBattleOver);
    }

    private boolean isInCombat() {
        return CardCrawlGame.isInARun() && AbstractDungeon.currMapNode != null
                && AbstractDungeon.getCurrRoom() != null
                && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT;
    }

    private boolean isDead() {
        return AbstractDungeon.isScreenUp && AbstractDungeon.screen == AbstractDungeon.CurrentScreen.DEATH;
    }

    private void initEvaluation() {
        combatFailed = false;

        readFitnessFunction(FITNESS_FILE);
        writeFitnessFunctionForAgent();

        CommandAutomator.readCommandsEval();
        CommandAutomator.runInitCommands();

        startingDeck = new ArrayList<>(AbstractDungeon.player.masterDeck.group);
        waitingForDeckUpdate = true;

        writeOutputHeader();
    }

    private void toggleFast(boolean isFast) {
        if (!ALLOW_FAST_MODE) return;

        if (isFast) {
            FastActionPatches.enableFastMode();
        }
        else {
            Settings.ACTION_DUR_XFAST = 0.1F;
            Settings.ACTION_DUR_FASTER = 0.2F;
            Settings.ACTION_DUR_FAST = 0.25F;
            Settings.ACTION_DUR_MED = 0.5F;
            Settings.ACTION_DUR_LONG = 1.0F;
            Settings.ACTION_DUR_XLONG = 1.5F;
            FastActionPatches.disableFastMode();
        }
    }

    private boolean checkDeckUpdated() {
        ArrayList<AbstractCard> newDeck =
                new ArrayList<>(AbstractDungeon.player.masterDeck.group);

        if (newDeck.isEmpty()) return false;

        if (newDeck.size() != startingDeck.size()) return true;

        Map<String, Integer> countMap = new HashMap<>();

        for (AbstractCard card : startingDeck) {
            String key = card.cardID;
            countMap.put(key, countMap.getOrDefault(key, 0) + 1);
        }

        for (AbstractCard card : newDeck) {
            String key = card.cardID;

            if (!countMap.containsKey(key)) return true;

            countMap.put(key, countMap.get(key) - 1);

            if (countMap.get(key) == 0) {
                countMap.remove(key);
            }
        }

        return !countMap.isEmpty();
    }

    private void startNewCombat() {
        combatFailed = false;

        if (CommandAutomator.hasNextFight()) {
            System.out.println("Evaluating fight: " + CommandAutomator.getCurrentFight());
            writeFitnessFunctionForAgent();
            restartFight();
        }
        else {
            System.out.println("Finished all simulations");
            simRunning = false;
            toggleFast(false);
        }

        AbstractDungeon.actionManager.actions.clear();
        AbstractDungeon.actionManager.monsterAttacksQueued = true;
        AbstractDungeon.actionManager.monsterQueue.clear();
    }

    private void restartFight() {
        startingState.loadState();
        waitingForCombatToBattle = true;
    }

    private void startNextCombat() {
        System.out.println("Going to next fight: " + CommandAutomator.getCurrentFight());
        CommandAutomator.restartCurrentFight();
        waitingForCombatToSave = true;
    }

    private void writeCombatResult() {
        SaveState endState = new SaveState();

        int playerHealth = endState.getPlayerHealth();
        int enemiesRemaining = ValueFunctionManager.getAliveMonsterCount(endState);
        int enemyHealthSum = ValueFunctionManager.getTotalMonsterHealth(endState);
        int turnCount = endState.turn;
        boolean won = playerHealth > 0 && enemiesRemaining == 0;

        double fitnessFitness = combatFailed
                ? VERY_BAD_SCORE
                : calculateFitnessFitness(endState);

        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(OUTPUT_FILE),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            writer.write(
                    fitnessType.name() + ","
                            + csv(CommandAutomator.getCurrentFight()) + ","
                            + fitnessFitness + ","
                            + turnCount + ","
                            + playerHealth + ","
                            + enemiesRemaining + ","
                            + enemyHealthSum + ","
                            + won
            );
            writer.newLine();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to write combat evaluation result", e);
        }

        CommandAutomator.advanceNextFight();

        if (CommandAutomator.hasNextFight()) {
            waitingForCombatReset = true;
            startingState.loadState();
        }
        else {
            System.out.println("Finished evaluating fitness function");
            simRunning = false;
            toggleFast(false);
        }
    }

    private double calculateFitnessFitness(SaveState endState) {
        int playerHealth = endState.getPlayerHealth();
        int healthLost = startingState.getPlayerHealth() - endState.getPlayerHealth();
        int turnCount = endState.turn;
        int enemiesLeft = ValueFunctionManager.getAliveMonsterCount(endState);
        int enemyHealthLeft = ValueFunctionManager.getTotalMonsterHealth(endState);

        double winBonus = playerHealth > 0 ? 1000.0 : 0.0;

        double turnTerm = (playerHealth > 0)
                ? -turnCount * 2.0
                : Math.sqrt(turnCount) * 2.0;

        return winBonus
                + playerHealth
                - healthLost * 5.0
                + turnTerm
                - enemiesLeft * 25.0
                - enemyHealthLeft * 2.0;
    }

    private void readFitnessFunction(String fileName) {
        File file = new File(fileName);

        if (!file.exists()) {
            throw new RuntimeException("Could not find fitness file at: " + file.getAbsolutePath());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            String body = reader.readLine();

            if (header == null || body == null) {
                throw new RuntimeException("Fitness file must contain a type line and a fitness function line.");
            }

            switch (header.trim()) {
                case "WEIGHTED_SUM":
                    fitnessType = FitnessType.WEIGHTED_SUM;
                    fitnessFunction = new WeightedSumFitness(body.trim());
                    break;

                case "EXPRESSION_TREE":
                    fitnessType = FitnessType.EXPRESSION_TREE;
                    fitnessFunction = new CompatExpression(body.trim());
                    break;

                default:
                    throw new RuntimeException("Unknown fitness type: " + header);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read fitness function file", e);
        }
    }

    private void writeFitnessFunctionForAgent() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FITNESS_FILE))) {
            writer.write(fitnessType.name());
            writer.newLine();
            writer.write(fitnessFunction.toString());
            writer.newLine();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to write current fitness function file", e);
        }
    }

    private void writeOutputHeader() {
        try {
            Files.write(
                    Paths.get(OUTPUT_FILE),
                    ("fitnessType,combat,fitnessFitness,turnCount,playerHealth,enemiesRemaining,enemyHealthSum,won\n").getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to initialize output file", e);
        }
    }

    private String csv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}