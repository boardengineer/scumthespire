package battleaimod.battleai;

import ludicrousspeed.Controller;
import ludicrousspeed.simulator.commands.Command;
import savestate.CardState;
import savestate.SaveState;

import java.util.*;
import java.util.stream.Collectors;

public class BattleAiController implements Controller {
    public int maxTurnLoads = 15_000;

    public int targetTurn;
    public int targetTurnJump;

    public PriorityQueue<TurnNode> turns = new PriorityQueue<>();

    public int minDamage;
    public StateNode bestEnd;

    // If it doesn't work out just send back a path to kill the players o the game doesn't get
    // stuck.
    public StateNode deathNode = null;

    // The state the AI is currentl processing from
    public TurnNode committedTurn = null;

    // The target turn that will be loaded if/when the max turn loads is hit
    public TurnNode bestTurn = null;
    public TurnNode backupTurn = null;

    public int startingHealth;
    public boolean isDone = false;
    public final SaveState startingState;
    private boolean initialized;

    public TurnNode curTurn;

    public int turnsLoaded = 0;

    public long controllerStartTime;

    public HashMap<String, Long> runTimes;

    public BattleAiController(SaveState state) {
        runTimes = new HashMap<>();
        targetTurn = 8;
        targetTurnJump = 6;

        minDamage = 5000;
        bestEnd = null;
        startingState = state;
        initialized = false;
        startingState.loadState();
    }

    public void step() {
        if (isDone) {
            return;
        }
            if (!initialized) {
                TurnNode.nodeIndex = 0;
                initialized = true;
                isDone = false;
                StateNode firstStateContainer = new StateNode(null, null, this);
                startingHealth = startingState.getPlayerHealth();
                firstStateContainer.saveState = startingState;
                turns = new PriorityQueue<>();
                turns.add(new TurnNode(firstStateContainer, this, null));

                controllerStartTime = System.currentTimeMillis();
                runTimes = new HashMap<>();
                CardState.resetFreeCards();
            }

            if ((turns
                    .isEmpty() || turnsLoaded >= maxTurnLoads) && (curTurn == null || curTurn.isDone)) {
                if (bestEnd != null) {
                    System.err.println("Found end at turn threshold, going into rerun");

                    // uncomment to get tree files
                    // showTree();
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

                    // TODO this is here to prevent playback errors
                    bestEnd = null;
                    minDamage = 5000;

                    return;
                }
            }

            while (!turns.isEmpty() && curTurn == null) {
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

            if (turns.isEmpty()) {
                System.err.println("turns is empty");
                if (curTurn != null && curTurn.isDone && bestEnd != null && (bestTurn == null || minDamage <= 0)) {
                    System.err.println("found end, going into rerunmode");
                    startingState.loadState();

                    // uncomment for tree files
                    //showTree();
                    printRuntimeStats();

                    isDone = true;
                    return;
                } else {
                    System.err
                            .println("not done yet death node:" + deathNode + "\nbest turn:" + bestTurn + "\ncurTurn:" + curTurn);
                }
            } else if (curTurn != null) {
                long startTurnStep = System.currentTimeMillis();

                boolean reachedNewTurn = curTurn.step();
                if (reachedNewTurn) {
                    curTurn = null;
                }

                addRuntime("Battle AI TurnNode Step", System.currentTimeMillis() - startTurnStep);
            }

            if ((curTurn == null || curTurn.isDone || bestTurn != null) && turns.isEmpty()) {
                if (curTurn == null || TurnNode
                        .getTotalMonsterHealth(curTurn) != 0 && bestTurn != null) {
                    System.err
                            .println("Loading for turn completion threshold, best turn: " + bestTurn);
                    turnsLoaded = 0;
                    turns.clear();
                    turns.add(bestTurn);
                    targetTurn += targetTurnJump;
                    bestTurn.startingState.saveState.loadState();
                    committedTurn = bestTurn;
                    bestTurn = null;
                    backupTurn = null;
                }
            }

            if (deathNode != null && turns
                    .isEmpty() && bestTurn == null && (curTurn == null || curTurn.isDone)) {
                System.err.println("Sending back death turn");
                bestEnd = deathNode;
                isDone = true;
                return;
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
        System.err.println(runTimes.entrySet()
                                   .stream()
                                   .map(entry -> entry.toString())
                                   .sorted()
                                   .collect(Collectors.joining("\n")));
        System.err.println("-------------------------------------------------------------------");
    }

    public void addRuntime(String name, long amount) {
        if (!runTimes.containsKey(name)) {
            runTimes.put(name, amount);
        } else {
            runTimes.put(name, amount + runTimes.get(name));
        }
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
}
