package battleaimod.battleai;

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
    private int maxTurnLoads = 300;

    public int targetTurn;
    public int targetTurnJump;

    public PriorityQueue<TurnNode> turns = new PriorityQueue<>();
    public StateNode root = null;

    public int minDamage = 5000;
    public StateNode bestEnd = null;
    public TurnNode bestTurn = null;

    public int startingHealth;
    public boolean isDone = false;
    public final SaveState startingState;
    private boolean initialized = false;
    public Iterator<Command> bestPathRunner;
    public TurnNode curTurn;

    public int turnsLoaded = 0;
    public TurnNode furthestSoFar = null;

    public boolean runCommandMode = false;
    public boolean runPartialMode = false;

    private final boolean shouldRunWhenFound;

    public BattleAiController(SaveState state) {
        targetTurn = 4;
        targetTurnJump = 3;

        if (state.encounterName.equals("Lagavulin")) {
            maxTurnLoads = 500;
            targetTurn = 2;
            targetTurnJump = 3;
        } else if (state.encounterName.equals("Gremlin Nob")) {
            targetTurn = 2;
            targetTurnJump = 3;
        } else if (state.encounterName.equals("The Guardian")) {
            maxTurnLoads = 300;
            targetTurn = 2;
            targetTurnJump = 2;
        } else if (state.encounterName.equals("Hexaghost")) {
            maxTurnLoads = 300;
            targetTurn = 2;
            targetTurnJump = 2;
        } else if(state.encounterName.equals("Gremlin Gang")) {
            maxTurnLoads = 300;
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
        bestPathRunner = commands.iterator();
        startingState = saveState;
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
        if (isDone) {
            return;
        }
        if (!runCommandMode && !runPartialMode) {
            if (turnsLoaded >= maxTurnLoads && (curTurn == null || curTurn.isDone)) {
                if (bestTurn != null) {
                    System.err.println("Loading for turn load threshold, best turn: " + bestTurn);
                    turnsLoaded = 0;
                    turns.clear();
                    turns.add(bestTurn);
                    targetTurn += targetTurnJump;
                    bestTurn.startingState.saveState.loadState();
                    bestTurn = null;
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

                // TODO how does this jump?
                if (turnNumber >= targetTurn) {
                    if (bestTurn == null || curTurn.isBetterThan(bestTurn)) {
                        bestTurn = curTurn;
                    }

                    curTurn = null;
                    ++turnsLoaded;
                    turns.poll();
                } else {
                    System.err.println("the best turn has damage " + curTurn + " " + turns
                            .size() + " " + (++turnsLoaded));
                    if (curTurn.isDone) {
                        System.err.println("finished turn");
                        turns.poll();
                    }
                }
            }

            if (turns.isEmpty()) {
                System.err.println("turns is empty");
                if (curTurn != null && curTurn.isDone && bestEnd != null && bestTurn == null) {
                    System.err.println("found end, going into rerunmode");

                    ArrayList<Command> commands = new ArrayList<>();
                    StateNode iterator = bestEnd;
                    while (iterator != null) {
                        commands.add(0, iterator.lastCommand);
                        iterator = iterator.parent;
                    }

                    startingState.loadState();
                    bestPathRunner = commands.iterator();

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
                    turnsLoaded = 0;
                    turns.clear();
                    turns.add(bestTurn);
                    targetTurn += targetTurnJump;
                    bestTurn.startingState.saveState.loadState();
                    bestTurn = null;
                }
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
                turns = new PriorityQueue<>();
                root = null;
                minDamage = 5000;
                bestEnd = null;
                isDone = true;
            }
        }
    }
}
