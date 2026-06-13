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
import com.megacrit.cardcrawl.vfx.ThoughtBubble;
import ludicrousspeed.LudicrousSpeedMod;
import savestate.SaveState;
import savestate.monsters.MonsterState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class EvolutionManager implements PostUpdateSubscriber {
    private boolean simRunning = false;
    public static boolean canRunAutoBattler = false;
    private static final Random rand = new Random();
    private List<AbstractFitness> population = new ArrayList<>();
    private int currentFitnessIndex;

    private final int MIN_POPULATION = 10;
    private final int ELITES = 3;

    private SaveState startingState;

    private boolean waitingForCombatToSave = false;
    private boolean waitingForCombatToBattle = false;
    private boolean waitingForDeckUpdate = false;
    private boolean waitingForCombatReset = false;

    private ArrayList<AbstractCard> startingDeck;
    private int startingHp;
    private int startingTurn;

    private static final List<AbstractCard> cardsPlayed = new ArrayList<>();

    private final boolean ALLOW_FAST_MODE = true;
    private boolean currentlyFast = false;

    public boolean combatFailed = false;
    private final double VERY_BAD_SCORE = -100000000;

    public static boolean hasTriggeredThisTurn = false;

    private int iterations = 0;

    private enum FitnessType{
        WEIGHTED_SUM,
        EXPRESSION_TREE;
    }

    private FitnessType fitnessType;

    @Override
    public void receivePostUpdate() {
        if (!simRunning && Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            System.out.println("starting sim");
            simRunning = true;
            initEvolution();
            return;
        }

        if(!simRunning) return;

        if(waitingForDeckUpdate){
            if(checkDeckUpdated()){
                toggleFast(true);
                startingHp = AbstractDungeon.player.currentHealth;
                waitingForDeckUpdate = false;
                CommandAutomator.restartCurrentFight();
                waitingForCombatToSave = true;
            }
            return;
        }

        if(waitingForCombatToSave){
            if(isInCombat()){
                startingState = new SaveState();
                startingTurn = GameActionManager.turn;
                waitingForCombatToSave = false;
                startNewCombat();
            }
            return;
        }

        if (waitingForCombatReset) {
            if(AbstractDungeon.player.currentHealth == startingHp && GameActionManager.turn == startingTurn){
                waitingForCombatReset = false;
                startNextGenCombat();
            }
            return;
        }

        if(waitingForCombatToBattle){
            if(isInCombat()){
                EvolutionManager.canRunAutoBattler = true;
                waitingForCombatToBattle = false;
            }
            return;
        }

        if(EvolutionManager.canRunAutoBattler){
            if(combatOver()){ //detect combat is over
                EvolutionManager.canRunAutoBattler = false;
                System.out.println("combat over detected!!");

                if(combatFailed){
                    population.get(currentFitnessIndex).setFitnessFitness(VERY_BAD_SCORE);

                    AbstractDungeon.overlayMenu.endTurnButton.disable(true);
                    hasTriggeredThisTurn = false;

                }
                else{
                    population.get(currentFitnessIndex).setFitnessFitness(calculateFitnessFitness());
                }

                startNewCombat();
            }
        }
    }

    public void failCombat(){
        combatFailed = true;
        AbstractDungeon.actionManager.addToTop(new LoseHPAction(AbstractDungeon.player, AbstractDungeon.player, 999));
    }

    //TODO: check if this works when combat ends and in reward screen
    private boolean combatOver() {
        return  isDead() || (isInCombat() && (AbstractDungeon.getCurrRoom().isBattleOver));
    }

    private boolean isInCombat() {
        return CardCrawlGame.isInARun() && AbstractDungeon.currMapNode != null
                && AbstractDungeon.getCurrRoom() != null
                && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT;
    }

    private boolean isDead(){
        //return AbstractDungeon.player.isDead || AbstractDungeon.player.isDying;
        return (AbstractDungeon.isScreenUp && AbstractDungeon.screen == AbstractDungeon.CurrentScreen.DEATH);
    }

    private void initEvolution(){
        combatFailed = false;
        currentFitnessIndex = -1;
        getPopulationFromFile("FitnessFunctions.txt");
        CommandAutomator.readCommands();

        CommandAutomator.runInitCommands();
        startingDeck = new ArrayList<>(AbstractDungeon.player.masterDeck.group);
        waitingForDeckUpdate = true;

        ValueFunctionManager.writeVariablesToFile("ipc/FeatureBank.txt");
    }

    private void toggleFast(boolean isFast) {
        if(!ALLOW_FAST_MODE) return;

        if(isFast){
            //BattleAiMod.goFast = true;
            //SaveStateMod.shouldGoFast = true;
            //LudicrousSpeedMod.plaidMode = true;

            FastActionPatches.enableFastMode();
        }
        else {
//            //BattleAiMod.goFast = false;
//            //SaveStateMod.shouldGoFast = false;
//            LudicrousSpeedMod.plaidMode = false;
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

        //System.out.println("deck size == " + newDeck.size());
        if(newDeck.isEmpty()) return false;

        // Different sizes → definitely different
        if (newDeck.size() != startingDeck.size()) return true;

        Map<String, Integer> countMap = new HashMap<>();

        // Count starting deck
        for (AbstractCard card : startingDeck) {
            String key = card.cardID;
            countMap.put(key, countMap.getOrDefault(key, 0) + 1);
        }

        // Subtract using new deck
        for (AbstractCard card : newDeck) {
            String key = card.cardID;

            if (!countMap.containsKey(key)) return true;

            countMap.put(key, countMap.get(key) - 1);

            if (countMap.get(key) == 0) {
                countMap.remove(key);
            }
        }

        // If empty → exact match
        return !countMap.isEmpty();
    }

    private void startNewCombat(){
        cardsPlayed.clear();
        combatFailed = false;
        currentFitnessIndex++;
        if(currentFitnessIndex == population.size()){
            //finished all fitness functions
            System.out.println("Finished all fights for: " + CommandAutomator.getCurrentFight());
            CommandAutomator.advanceNextFight();
            if(CommandAutomator.hasNextFight()){

                System.out.println("Sorting and Evolving next gen");
                //sort
                Collections.sort(population);

                //write temp just in case of program crash
                writePopulationToFile("NewFitnessFunctions.txt");

                //evolve next gen
                evolvePopulation();

                // go to next combat:

                System.out.println("Resetting state");
                waitingForCombatReset = true;
                startingState.loadState();

            }
            else {
                //No more combats in the command list -> FINISHED, write to file and end
                System.out.println("Finished all simulations");
                Collections.sort(population);
                writePopulationToFile("NewFitnessFunctions.txt");
                simRunning = false;
                toggleFast(false);
            }
        }
        else {
            //still have fitness funcs to check
            writeFitnessFunction();
            restartFight();
        }

        AbstractDungeon.effectList.add(new ThoughtBubble(AbstractDungeon.player.dialogX, AbstractDungeon.player.dialogY, 5.0F, "Current Fitness Index: " + currentFitnessIndex, true));

        //TODO: test if actions that trigger after death still trigger on next run
        AbstractDungeon.actionManager.actions.clear();
        AbstractDungeon.actionManager.monsterAttacksQueued = true;
        AbstractDungeon.actionManager.monsterQueue.clear();
    }

    private void evolvePopulation() {
        switch (fitnessType){
            case EXPRESSION_TREE:
                evolveExpressionTree();
                break;
            case WEIGHTED_SUM:
                evolveWeightedSum();
                break;
        }
        iterations++;
    }

    private void evolveWeightedSum(){
        List<WeightedSumFitness> population = this.population.stream()
                .filter(f -> f instanceof WeightedSumFitness)
                .map(f -> (WeightedSumFitness) f)
                .collect(Collectors.toList());

        // Assume population is already sorted by fitness (best first)
        List<AbstractFitness> newPopulation = new ArrayList<>();

        // 1. Elitism (copy top ELITES without mutation)
        for (int i = 0; i < ELITES && i < population.size(); i++) {
            newPopulation.add(new WeightedSumFitness(population.get(i), false));
        }

        // 2. Generate rest of population
        while (newPopulation.size() < MIN_POPULATION) {
            // Select two parents (simple random selection)
            WeightedSumFitness parent1 = population.get(rand.nextInt(population.size()));
            WeightedSumFitness parent2;
            do {
                parent2 = population.get(rand.nextInt(population.size()));
            } while (parent1 == parent2);

            // Crossover
            WeightedSumFitness child = new WeightedSumFitness(parent1, parent2);

            // Mutation
            child = new WeightedSumFitness(child, true);

            newPopulation.add(child);
        }

        // 3. Replace old population
        this.population = newPopulation;
    }

    private void evolveExpressionTree() {
        File outFile = new File("ipc/ModOutput.txt");
        File inFile = new File("ipc/JeneticsOutput.txt");

        //Write file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            for (AbstractFitness individual : population) {
                writer.write("FITNESS=" + individual.getFitnessFitness());
                writer.newLine();

                writer.write(individual.toString());
                writer.newLine();
            }

            writer.write("READY");
            writer.newLine();
            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException("Failed to write population for evolution", e);
        }

        //Read
        while (true) {
            if (inFile.exists()) {
                try {
                    List<String> lines = Files.readAllLines(inFile.toPath());

                    int end = lines.size();
                    while (end > 0 && lines.get(end - 1).trim().isEmpty()) {
                        end--;
                    }

                    if (end > 0 && "READY".equals(lines.get(end - 1).trim())) {
                        List<AbstractFitness> newPopulation = new ArrayList<>();
                        Set<String> seenExpressions = new HashSet<>();

                        for (int i = 0; i < end - 1; i++) {
                            String expr = lines.get(i).trim();

                            if (expr.isEmpty()) {
                                continue;
                            }

                            if (seenExpressions.add(expr)) {
                                newPopulation.add(new CompatExpression(expr));
                            }
                        }

                        population.clear();
                        population.addAll(newPopulation);

                        try (BufferedWriter clearWriter = new BufferedWriter(new FileWriter(inFile, false))) {
                            clearWriter.write("");
                        }

                        break;
                    }

                } catch (IOException e) {
                    throw new RuntimeException("Failed to read evolved population", e);
                }
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void writeFitnessFunction() {
        AbstractFitness current = population.get(currentFitnessIndex);
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("CurrentFitnessValues.txt"))) {
            writer.write(fitnessType.name());
            writer.newLine();
            writer.write(current.toString());
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write fitness file", e);
        }
    }

    private void restartFight(){
        startingState.loadState();
        waitingForCombatToBattle = true;
    }

    private void getPopulationFromFile(String fileName) {
        population.clear();

        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("Could not find the command file at: " + file.getAbsolutePath());
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String header = reader.readLine();

            if (header == null) {
                System.out.println("Empty population file.");
                return;
            }

            switch (header.trim()) {

                case "WEIGHTED_SUM":
                    fitnessType = FitnessType.WEIGHTED_SUM;
                    readWeightedSumPopulation(reader);
                    break;

                case "EXPRESSION_TREE":
                    fitnessType = FitnessType.EXPRESSION_TREE;
                    readExpressionTreePopulation(reader);
                    break;

                default:
                    System.out.println("Unknown population type: " + header);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readWeightedSumPopulation(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();

            // Skip empty lines
            if (line.isEmpty()) continue;

            population.add(new WeightedSumFitness(line));
        }

        // Pad population if too small
        while (population.size() < MIN_POPULATION) {
            WeightedSumFitness parent = (WeightedSumFitness) population.get(rand.nextInt(population.size()));
            population.add(new WeightedSumFitness(parent, true));
        }
    }

    private void readExpressionTreePopulation(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();

            // Skip empty lines
            if (line.isEmpty()) continue;

            try{
                population.add(new CompatExpression(line));
            }
            catch(Exception e){
                System.out.println("line failed to parse: " + line);
                throw new RuntimeException(e);
            }
        }
    }

    private void writePopulationToFile(String fileName) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(fileName),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            writer.write("Iterations: " + iterations);
            writer.newLine();

            for (AbstractFitness individual : population) {
                writer.write(individual.toString());
                writer.newLine();
            }

            writer.newLine(); // optional: separate runs

        } catch (IOException e) {
            throw new RuntimeException("Failed to write population to file: " + fileName, e);
        }
    }


    private double calculateFitnessFitness(){
        SaveState endState = new SaveState();

        //TODO: maybe need server to send back additional info that save states can't see
        //wasted block, energy etc.

        int playerHealth = endState.getPlayerHealth();
        int healthLost = startingState.getPlayerHealth() - endState.getPlayerHealth();
        int turnCount = endState.turn;
        int enemiesLeft = ValueFunctionManager.getAliveMonsterCount(endState);
        int enemyHealthLeft = ValueFunctionManager.getTotalMonsterHealth(endState);

        double winBonus = playerHealth > 0 ? 1000.0 : 0.0;

        double turnTerm = (playerHealth > 0)
                ? -turnCount * 2.0             // penalize slow fights
                : +Math.sqrt(turnCount) * 2.0; // weakly reward lasting longer if you lose, with diminishing returns

        return winBonus
                + playerHealth * 1.0   // reward ending health
                - healthLost * 5.0   // penalize damage taken
                + turnTerm
                - enemiesLeft * 25.0 // penalize enemies left alive
                - enemyHealthLeft * 2.0 //penalize remaining enemy health
                ;
    }

    private void startNextGenCombat(){
        System.out.println("Going to next fight: " + CommandAutomator.getCurrentFight());
        CommandAutomator.restartCurrentFight();
        waitingForCombatToSave = true;
        currentFitnessIndex = -1;
    }

    public static void addCardPlayed(AbstractCard c){
        EvolutionManager.cardsPlayed.add(c);
    }

}
