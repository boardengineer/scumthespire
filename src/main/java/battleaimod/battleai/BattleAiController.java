package battleaimod.battleai;

import battleaimod.BattleAiMod;
import battleaimod.ChoiceScreenUtils;
import battleaimod.savestate.SaveState;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class BattleAiController {
    public static String currentEncounter = null;
    public int maxTurnLoads = 100;

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

    public BattleAiController(SaveState state) {
        targetTurn = 4;
        targetTurnJump = 3;

        if (state.encounterName == null) {
        } else if (state.encounterName.equals("Lagavulin")) {
            maxTurnLoads = 200;
            targetTurn = 2;
            targetTurnJump = 3;
        } else if (state.encounterName.equals("Gremlin Nob")) {
            targetTurn = 2;
            targetTurnJump = 3;
        } else if (state.encounterName.equals("The Guardian")) {
            targetTurn = 2;
            targetTurnJump = 2;
        } else if (state.encounterName.equals("Hexaghost")) {
            targetTurn = 2;
            targetTurnJump = 2;
        } else if (state.encounterName.equals("Gremlin Gang")) {
            targetTurnJump = 2;
        } else if (state.encounterName.equals("Champ")) {
            targetTurn = 2;
            targetTurnJump = 2;
        }

        minDamage = 5000;
        bestEnd = null;
        shouldRunWhenFound = false;
        startingState = state;
        initialized = false;
        startingState.loadState();
    }

    public BattleAiController(SaveState state, boolean shouldRunWhenFound) {
        minDamage = 5000;
        bestEnd = null;
        this.shouldRunWhenFound = shouldRunWhenFound;
        startingState = state;
        initialized = false;
        startingState.loadState();
    }

    public BattleAiController(SaveState saveState, List<Command> commands) {
        runCommandMode = true;
        shouldRunWhenFound = true;
        bestPath = commands;
        bestPathRunner = commands.iterator();
        startingState = saveState;
    }

    public BattleAiController(SaveState saveState, List<Command> commands, boolean isComplete) {
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
        return shouldCheckForPlays() || isEndCommandAvailable() || !ChoiceScreenUtils
                .getCurrentChoiceList().isEmpty();
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
        if (!shouldGoFast()) {
            System.err.println("step");
        }
        if (isDone) {
            return;
        }
        if (!runCommandMode && !runPartialMode) {
            if (turnsLoaded >= maxTurnLoads && (curTurn == null || curTurn.isDone)) {
                if (bestEnd != null) {
                    System.err.println("Found end at turn treshold, going into rerun");
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
                } else if (turnsLoaded >= maxTurnLoads * 1.5 && backupTurn != null) {
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

            GameActionManager s;
            long currentTime = System.nanoTime();

            if (!initialized) {
                initialized = true;
                runCommandMode = false;
                StateNode firstStateContainer = new StateNode(null, null, this);
                startingHealth = startingState.getPlayerHealth();
                root = firstStateContainer;
                firstStateContainer.saveState = startingState;
                turns = new PriorityQueue<>();
                turns.add(new TurnNode(firstStateContainer, this));
            }

            while (!turns
                    .isEmpty() && (curTurn == null || (curTurn.isDone || curTurn.startingState.saveState.turn >= targetTurn))) {
                curTurn = turns.peek();

                int turnNumber = curTurn.startingState.saveState.turn;

                if (turnNumber >= targetTurn) {
                    if (bestTurn == null || curTurn.isBetterThan(bestTurn)) {
                        bestTurn = curTurn;
                    }

                    curTurn = null;
                    ++turnsLoaded;
                    turns.poll();
                } else {
                    System.err.println("the best turn has damage " + curTurn + " " + turns
                            .size() + " " + (turnsLoaded));
                    if (curTurn.isDone) {
                        System.err.println("finished turn");
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
                    runCommandMode = true;
                    return;
                } else {
                    System.err.println("not done yet");
                }
            } else if (curTurn != null) {
                boolean reachedNewTurn = curTurn.step();
                if (reachedNewTurn) {
                    curTurn = null;
                }
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
        return new TurnNode(stateNode, this);
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
}
