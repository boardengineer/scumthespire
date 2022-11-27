package battleaimod.battleai;

import FightPredictor.FightPredictor;
import FightPredictor.ml.ModelUtils;
import FightPredictor.patches.com.megacrit.cardcrawl.combat.CombatPredictionPatches;
import FightPredictor.util.BaseGameConstants;
import basemod.BaseMod;
import battleaimod.ValueFunctions;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import ludicrousspeed.Controller;
import ludicrousspeed.simulator.commands.Command;
import savestate.CardState;
import savestate.SaveState;
import savestate.SaveStateMod;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static savestate.SaveStateMod.addRuntime;

public class BattleAiController implements Controller {
    private final int maxTurnLoads;

    public int targetTurn;
    public int targetTurnJump;

    public PriorityQueue<TurnNode> turns = new PriorityQueue<>();

    // The best winning result unless the AI gave up in which case it will contain the chosen death
    // path
    public StateNode bestEnd;

    // If it doesn't work out just send back a path to kill the players so the game doesn't get
    // stuck.
    public StateNode deathNode = null;

    // The state the AI is currently processing from
    public TurnNode committedTurn = null;

    // The target turn that will be loaded if/when the max turn loads is hit
    public TurnNode bestTurn = null;
    public TurnNode backupTurn = null;

    public int startingHealth;
    public boolean isDone = false;
    public final SaveState startingState;
    private boolean initialized;

    // EXPERIMENTAL
    private TurnNode startNode = null;
    public static final boolean SHOULD_SHOW_TREE = false;

    // The turn we're currently processing, if this is null then a turn will be polled from the
    // pqueue.
    public TurnNode curTurn;

    // The count of turns restricted by a turn limit.
    public int turnsLoaded = 0;

    private long startTime = 0;
    public int expectedDamage = 0;

    public BattleAiController(SaveState state, int maxTurnLoads) {
        SaveStateMod.runTimes = new HashMap<>();
        targetTurn = 8;
        targetTurnJump = 6;

        bestEnd = null;
        startingState = state;
        initialized = false;

        System.err.println("loading state from constructor");
        startingState.loadState();

        if (BaseMod.hasModID("FightPredictor:")) {
            try {
                float prediction = FightPredictor.model.predict(ModelUtils.getBaseInputVector());
                expectedDamage = MathUtils.round(prediction * 100);
            } catch (Exception e) {
                // This can happen either because the model doesn't support something or the mod isn't
                // installed
            }

            runPredictions();
        }

        this.maxTurnLoads = maxTurnLoads;
    }

    public void runPredictions() {
        float prediction = FightPredictor.model.predict(ModelUtils.getBaseInputVector());
        float intPrediction = Math.round(prediction * 100);
        CombatPredictionPatches.combatStartingHP = AbstractDungeon.player.currentHealth;
        CombatPredictionPatches.combatHPLossPrediction = MathUtils.round(prediction * 100);

        // Set up and do all card copying here
        // Only use thread safe things inside the other thread (maybe convert as much stuff to strings as possible)
        // That might mean overloading other methods from other classes to take strings, since they use them under
        // the hood anyways

        long start = System.currentTimeMillis();

        // Get the character's card pool
        ArrayList<AbstractCard> unupgradedCards = new ArrayList<>();
        switch (AbstractDungeon.player.chosenClass) {
            case IRONCLAD:
                CardLibrary.addRedCards(unupgradedCards);
                break;
            case THE_SILENT:
                CardLibrary.addGreenCards(unupgradedCards);
                break;
            case DEFECT:
                CardLibrary.addBlueCards(unupgradedCards);
                break;
            case WATCHER:
                CardLibrary.addPurpleCards(unupgradedCards);
                break;
            default:
                return;
        }

        // Make copies of cards to protect from concurency problems
        // Add the upgraded cards to the pool
        List<AbstractCard> cardPool = unupgradedCards.stream().map(AbstractCard::makeCopy)
                                                     .collect(Collectors.toList());
        List<AbstractCard> upgradedPool = cardPool.stream().map(AbstractCard::makeCopy)
                                                  .collect(Collectors.toList());
        upgradedPool.forEach(AbstractCard::upgrade);
        cardPool.addAll(upgradedPool);

        List<AbstractCard> playerCards = new ArrayList<>(AbstractDungeon.player.masterDeck.group)
                .stream().map(AbstractCard::makeCopy).collect(Collectors.toList());
        List<AbstractRelic> playerRelics = new ArrayList<>(AbstractDungeon.player.relics).stream()
                                                                                         .map(AbstractRelic::makeCopy)
                                                                                         .collect(Collectors
                                                                                                 .toList());
        int startingHealth = AbstractDungeon.player.currentHealth;
        int maxHealth = AbstractDungeon.player.maxHealth;

        // Get the enemies to predict against
        Set<String> elitesAndBosses = new HashSet<>();
        elitesAndBosses.addAll(BaseGameConstants.eliteIDs.get(AbstractDungeon.actNum));
        elitesAndBosses.add(AbstractDungeon.bossKey);
        if (AbstractDungeon.actNum < 4) {
            elitesAndBosses
                    .addAll(BaseGameConstants.elitesAndBossesByAct.get(AbstractDungeon.actNum + 1));
        }
        long end = System.currentTimeMillis();

        new Thread(() -> {
            FightPredictor
                    .getPercentiles(cardPool, playerCards, playerRelics, startingHealth, maxHealth, elitesAndBosses);

            System.err.println(" predictions " + FightPredictor.percentiles.entrySet());
        }).start();
    }

    public void step() {
        if (isDone) {
            return;
        }
        if (!initialized) {
            TurnNode.nodeIndex = 0;
            startTime = System.currentTimeMillis();
            initialized = true;
            isDone = false;
            StateNode firstStateContainer = new StateNode(null, null, this);
            startingHealth = startingState.getPlayerHealth();
            firstStateContainer.saveState = startingState;
            turns = new PriorityQueue<>();
            startNode = new TurnNode(firstStateContainer, this, null);
            turns.add(startNode);

            SaveStateMod.runTimes = new HashMap<>();
            CardState.resetFreeCards();
        }

        if (curTurn == null || curTurn.isDone) {
            if (turns.isEmpty() || turnsLoaded >= maxTurnLoads) {
                if (bestEnd != null) {
                    System.err.println("Found end at turn threshold, going into rerun");
                    printRuntimeStats();

                    isDone = true;
                    return;
                } else if (bestTurn != null || backupTurn != null) {
                    if (bestTurn == null) {
                        System.err.println("Loading for backup " + backupTurn);
                        bestTurn = backupTurn;
                    }
                    System.err.println("Loading for turn load threshold, best turn: " + bestTurn);
                    turnsLoaded = 0;
                    turns.clear();

                    int backStep = targetTurnJump / 2;

                    TurnNode backStepTurn = bestTurn;
                    for (int i = 0; i < backStep; i++) {
                        if (backStepTurn == null) {
                            break;
                        }

                        backStepTurn = backStepTurn.parent;
                    }

                    if (backStepTurn != null && (committedTurn == null || backStepTurn.startingState.saveState.turn > committedTurn.startingState.saveState.turn)) {
                        bestTurn = backStepTurn;
                    }

                    System.err.println("Backstepping to turn: " + bestTurn);

                    TurnNode toAdd = makeResetCopy(bestTurn);
                    turns.add(toAdd);
                    targetTurn = bestTurn.startingState.saveState.turn + targetTurnJump;
                    toAdd.startingState.saveState.loadState();
                    committedTurn = toAdd;
                    bestTurn = null;
                    backupTurn = null;
                    deathNode = null;

                    // TODO this is here to prevent playback errors
                    bestEnd = null;

                    return;
                } else if (turns.isEmpty() || turnsLoaded >= maxTurnLoads * 10) {
                    if (deathNode != null) {
                        System.err.println("Sending back death turn");
                        bestEnd = deathNode;
                        isDone = true;
                        return;
                    }
                }
            }
        }


        while (!turns.isEmpty() && (curTurn == null || curTurn.isDone)) {
            curTurn = turns.peek();

            int turnNumber = curTurn.startingState.saveState.turn;

            if (turnNumber >= targetTurn) {
                if (bestTurn == null || curTurn.isBetterThan(bestTurn)) {
                    bestTurn = curTurn;
                }

                addRuntime("turnsLoaded", 1);
                curTurn = null;
                ++turnsLoaded;
                turns.poll();
            } else {
                if (curTurn.isDone) {
                    turns.poll();
                }
            }
        }

        if (curTurn != null) {
            long startTurnStep = System.currentTimeMillis();

//            System.err.println("Stepping Turn " + curTurn.turnLabel);
            boolean reachedNewTurn = curTurn.step();
            if (reachedNewTurn) {
                curTurn = null;
            }

            addRuntime("Battle AI TurnNode Step", System.currentTimeMillis() - startTurnStep);
        }
    }

    private static TurnNode makeResetCopy(TurnNode node) {
        StateNode stateNode = new StateNode(node.startingState.parent, node.startingState.lastCommand, node.controller);
        stateNode.saveState = node.startingState.saveState;
        return new TurnNode(stateNode, node.controller, node.parent);
    }

    public static List<Command> commandsToGetToNode(StateNode endNode) {
        ArrayList<Command> commands = new ArrayList<>();
        StateNode iterator = endNode;
        while (iterator != null) {
            commands.add(0, iterator.lastCommand);
            iterator = iterator.parent;
        }

        return commands;
    }

    public static List<StateNode> stateNodesToGetToNode(StateNode endNode) {
        ArrayList<StateNode> result = new ArrayList<>();
        StateNode iterator = endNode;
        while (iterator != null) {
            result.add(0, iterator);
            iterator = iterator.parent;
        }

        return result;
    }

    public void printRuntimeStats() {
        System.err.println("-------------------------------------------------------------------");
        System.err.println("total time: " + (System.currentTimeMillis() - startTime));
        System.err.println(SaveStateMod.runTimes.entrySet()
                                                .stream()
                                                .map(entry -> entry.toString())
                                                .sorted()
                                                .collect(Collectors.joining("\n")));
        System.err.println("-------------------------------------------------------------------");
    }

    public boolean isDone() {
        return isDone;
    }

    public TurnNode committedTurn() {
        return committedTurn;
    }

    public int turnsLoaded() {
        return turnsLoaded;
    }

    public int maxTurnLoads() {
        return maxTurnLoads;
    }

    private void showTree() throws IOException {
        try {
            FileWriter writer = new FileWriter("out.dot");

            writer.write("digraph battleTurns {\n");
            TurnNode start = startNode;
            LinkedList<TurnNode> bfs = new LinkedList<>();
            bfs.add(start);
            while (!bfs.isEmpty()) {
                TurnNode node = bfs.pollFirst();

                int playerDamage = ValueFunctions.getPlayerDamage(node);
                int monsterHealth = TurnNode.getTotalMonsterHealth(node);

                String nodeLabel = String
                        .format("player damage:%d monster health:%d", playerDamage, monsterHealth);


                double f = (double) node.turnLabel / 100.;

                int r = 0;
                int g = 0;
                int b = 0;
                double a = (1 - f) / 0.25;    //invert and group
                int X = (int) Math.floor(a);    //this is the integer part
                int Y = (int) Math.floor(255 * (a - X)); //fractional part from 0 to 255
                switch (X) {
                    case 0:
                        r = 255;
                        g = Y;
                        b = 0;
                        break;
                    case 1:
                        r = 255 - Y;
                        g = 255;
                        b = 0;
                        break;
                    case 2:
                        r = 0;
                        g = 255;
                        b = Y;
                        break;
                    case 3:
                        r = 0;
                        g = 255 - Y;
                        b = 255;
                        break;
                    case 4:
                        r = 0;
                        g = 0;
                        b = 255;
                        break;
                }

                writer.write(String
                        .format("%s [label=\"%s\" color=\"#%02X%02X%02X\" style=\"filled\"]\n", node.turnLabel, nodeLabel, r, g, b));
                node.children.forEach(child -> {
                    try {
                        ArrayList<Command> commands = new ArrayList<>();
                        StateNode iterator = child.startingState;
                        while (iterator != node.startingState) {
                            commands.add(0, iterator.lastCommand);
                            iterator = iterator.parent;
                        }
                        writer.write(String
                                .format("%s->%s [label=\"%s\"]\n", node.turnLabel, child.turnLabel, commands));
                    } catch (IOException e) {
                        System.err.println("writing failed");
                        e.printStackTrace();
                    }
                    bfs.add(child);
                });
            }

            writer.write("}\n");
            writer.close();

        } catch (IOException e) {
            System.err.println("file writing failed");
            e.printStackTrace();
        }
    }
}
