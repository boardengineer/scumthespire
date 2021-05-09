package battleaimod.battleai;

import battleaimod.BattleAiMod;
import battleaimod.battleai.commands.Command;
import battleaimod.patches.FastActionsPatch;
import battleaimod.savestate.CardState;
import battleaimod.savestate.SaveState;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class BattleAiController {
    public static String currentEncounter = null;
    public int maxTurnLoads = 10_000;

    public int targetTurn;
    public int targetTurnJump;

    public PriorityQueue<TurnNode> turns = new PriorityQueue<>();
    public StateNode root = null;

    public int minDamage = 5000;
    public StateNode bestEnd = null;

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
    private boolean initialized = false;

    public List<Command> bestPath;
    private List<Command> queuedPath;

    public Iterator<Command> bestPathRunner;
    public TurnNode curTurn;

    public int turnsLoaded = 0;
    private final int totalSteps = 0;
    public TurnNode furthestSoFar = null;

    boolean isComplete = true;
    boolean wouldComplete = true;

    public boolean runCommandMode = false;
    public boolean runPartialMode = false;

    private final boolean shouldRunWhenFound;

    private TurnNode rootTurn = null;

    public long controllerStartTime;
    public long actionTime;
    public long stepTime;
    public long updateTime;
    public long loadstateTime;
    public HashMap<Class, Long> actionClassTimes;

    public HashMap<String, Long> runTimes;

    public long playerLoadTime;
    public long roomLoadTime;

    public BattleAiController(SaveState state) {
        runTimes = new HashMap<>();
        targetTurn = 5;
        targetTurnJump = 5;

        minDamage = 5000;
        bestEnd = null;
        shouldRunWhenFound = false;
        startingState = state;
        initialized = false;
        startingState.loadState();
    }

    public BattleAiController(SaveState state, boolean shouldRunWhenFound) {
        runTimes = new HashMap<>();
        minDamage = 5000;
        bestEnd = null;
        this.shouldRunWhenFound = shouldRunWhenFound;
        startingState = state;
        initialized = false;
        startingState.loadState();
    }

    public BattleAiController(SaveState saveState, List<Command> commands) {
        runTimes = new HashMap<>();
        runCommandMode = true;
        shouldRunWhenFound = true;
        bestPath = commands;
        bestPathRunner = commands.iterator();
        startingState = saveState;
    }

    public BattleAiController(SaveState saveState, List<Command> commands, boolean isComplete) {
        runTimes = new HashMap<>();
        runCommandMode = true;
        this.isComplete = isComplete;
        shouldRunWhenFound = true;
        bestPath = commands;
        bestPathRunner = commands.iterator();
        startingState = saveState;
    }

    public void updateBestPath(List<Command> commands, boolean wouldComplete) {
        queuedPath = commands;
        if (!bestPathRunner.hasNext()) {
            Iterator<Command> oldPath = bestPath.iterator();
            Iterator<Command> newPath = commands.iterator();

            while (oldPath.hasNext()) {
                oldPath.next();
                newPath.next();
            }

            bestPathRunner = newPath;
            this.isComplete = wouldComplete;
            bestPath = queuedPath;
        }

        this.wouldComplete = wouldComplete;
        this.runCommandMode = true;
    }

    public static boolean shouldStep() {
        return shouldCheckForPlays() || isEndCommandAvailable() || FastActionsPatch
                .shouldStepAiController();
    }

    public static boolean isInDungeon() {

        return CardCrawlGame.mode == CardCrawlGame.GameMode.GAMEPLAY && AbstractDungeon
                .isPlayerInDungeon() && AbstractDungeon.currMapNode != null;
    }

    private static boolean shouldCheckForPlays() {
        return isInDungeon() && (AbstractDungeon
                .getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && !AbstractDungeon.isScreenUp);
    }

    private static boolean isEndCommandAvailable() {
        return isInDungeon() && AbstractDungeon
                .getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && !AbstractDungeon.isScreenUp;
    }

    public void step() {
        if (runCommandMode) {
            System.err.println("running after run command mode");
        }
        if (isDone) {
            return;
        }
        if (!runCommandMode && !runPartialMode) {
            if (!initialized) {
                TurnNode.nodeIndex = 0;
                initialized = true;
                runCommandMode = false;
                StateNode firstStateContainer = new StateNode(null, null, this);
                startingHealth = startingState.getPlayerHealth();
                root = firstStateContainer;
                firstStateContainer.saveState = startingState;
                turns = new PriorityQueue<>();
                this.rootTurn = new TurnNode(firstStateContainer, this, null);
                turns.add(rootTurn);

                controllerStartTime = System.currentTimeMillis();
                actionTime = 0;
                stepTime = 0;
                updateTime = 0;
                loadstateTime = 0;
                playerLoadTime = 0;
                roomLoadTime = 0;
                actionClassTimes = new HashMap<>();
                runTimes = new HashMap<>();
                CardState.resetFreeCards();
            }

            if ((turns.isEmpty() || turnsLoaded >= maxTurnLoads) && (curTurn == null || curTurn.isDone)) {
                if (bestEnd != null) {
                    System.err.println("Found end at turn treshold, going into rerun");

                    // uncomment to get tree files
                    // showTree();
                    printRuntimeStats();

                    runCommandMode = true;
                    startingState.loadState();
                    bestPath = commandsToGetToNode(bestEnd);
                    bestPathRunner = bestPath.iterator();
                    return;
                } else if (bestTurn != null) {
                    System.err.println("Loading for turn load threshold, best turn: " + bestTurn);
                    turnsLoaded = 0;
                    turns.clear();
                    TurnNode toAdd = makeResetCopy(bestTurn);
                    turns.add(toAdd);
                    targetTurn += targetTurnJump;
                    toAdd.startingState.saveState.loadState();
                    committedTurn = toAdd;
                    bestTurn = null;
                    backupTurn = null;

                    // TODO this is here to prevent playback errors
                    bestEnd = null;
                    minDamage = 5000;


                    return;
                } else if (backupTurn != null) {
                    System.err.println("Loading from backup: " + backupTurn);
                    turnsLoaded = 0;
                    turns.clear();
                    TurnNode toAdd = makeResetCopy(backupTurn);
                    committedTurn = toAdd;
                    turns.add(toAdd);
                    toAdd.startingState.saveState.loadState();
                    bestTurn = null;
                    backupTurn = null;

                    // TODO this is here to prevent playback errors
                    bestEnd = null;
                    minDamage = 5000;

                    return;
                }
            }

            while (!turns
                    .isEmpty() && (curTurn == null || (curTurn.isDone || curTurn.startingState.saveState.turn >= targetTurn))) {
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
//                    System.err.println("the best turn has damage " + curTurn + " " + turns
//                            .size() + " " + (turnsLoaded));
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
                    bestPath = commandsToGetToNode(bestEnd);
                    bestPathRunner = bestPath.iterator();

                    // uncomment for tree files
                    //showTree();
                    printRuntimeStats();

                    runCommandMode = true;
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
                startingState.loadState();
                bestPath = commandsToGetToNode(deathNode);
                bestPathRunner = bestPath.iterator();
                runCommandMode = true;
                return;
            }

        }
        if (runCommandMode && shouldRunWhenFound) {
            boolean foundCommand = false;
            while (bestPathRunner.hasNext() && !foundCommand) {
                Command command = bestPathRunner.next();
                if (command != null) {
                    System.err.println(command);
                    foundCommand = true;
                    command.execute();
                } else {
                    foundCommand = true;
                    startingState.loadState();
                }
            }
            if (!shouldGoFast()) {
                AbstractDungeon.player.hand.refreshHandLayout();
            }

            if (!bestPathRunner.hasNext()) {
                System.err.println("no more commands to run");
                turns = new PriorityQueue<>();
                root = null;
                minDamage = 5000;
                bestEnd = null;
                BattleAiMod.readyForUpdate = true;

                if (isComplete) {
                    isDone = true;
                    runCommandMode = false;
                } else if (queuedPath != null && queuedPath.size() > bestPath.size()) {
                    System.err.println("Enqueueing path...");
                    Iterator<Command> oldPath = bestPath.iterator();
                    Iterator<Command> newPath = queuedPath.iterator();

                    while (oldPath.hasNext()) {
                        oldPath.next();
                        newPath.next();
                    }

                    bestPathRunner = newPath;
                    this.isComplete = wouldComplete;
                    bestPath = queuedPath;
                }
            }
        }
    }

    private TurnNode makeResetCopy(TurnNode node) {
        StateNode stateNode = new StateNode(node.startingState.parent, node.startingState.lastCommand, this);
        stateNode.saveState = node.startingState.saveState;
        return new TurnNode(stateNode, this, node.parent);
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

    private void showTree() {
        try {
            FileWriter writer = new FileWriter("out.dot");

            writer.write("digraph battleTurns {\n");
            TurnNode start = rootTurn;
            LinkedList<TurnNode> bfs = new LinkedList<>();
            bfs.add(start);
            while (!bfs.isEmpty()) {
                TurnNode node = bfs.pollFirst();

                int playerDamage = TurnNode.getPlayerDamage(node);
                int monsterHealth = TurnNode.getTotalMonsterHealth(node);

                String nodeLabel = String
                        .format("player damage:%d monster health:%d", playerDamage, monsterHealth);

                writer.write(String.format("%s [label=\"%s\"]\n", node.turnLabel, nodeLabel));
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

    public void printRuntimeStats() {
        System.err
                .printf("Total runtime: %d\taction time: %d\tstep time: %d\tupdate time:%d load time:%d\tplayer load:%d\troom load:%d\n", System
                        .currentTimeMillis() - controllerStartTime, actionTime, stepTime, updateTime, loadstateTime, playerLoadTime, roomLoadTime);
        System.err.println(actionClassTimes.entrySet().stream()
                                           .filter(entry -> entry.getValue() > 100)
                                           .sorted((e1, e2) -> (int) (e2.getValue() - e1
                                                   .getValue()))
                                           .map(entry -> String
                                                   .format("%s = %s", entry.getKey()
                                                                           .getSimpleName(), entry
                                                           .getValue()))
                                           .collect(Collectors.joining("\n")));
        System.err.println("-------------------------------------------------------------------");
        System.err.println(runTimes.entrySet().stream().map(entry -> entry.toString()).sorted()
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
}
