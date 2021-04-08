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

public class BattleAiController {
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
    private TurnNode curTurn;

    private int turnsLoaded = 0;
    public TurnNode furthestSoFar = null;

    public boolean runCommandMode = false;
    public boolean runPartialMode = false;

    private final boolean shouldRunWhenFound;

    public BattleAiController(SaveState state) {
        minDamage = 5000;
        bestEnd = null;
        shouldRunWhenFound = false;
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
            if (minDamage == 0) {
                System.err.println("we are done");
                runCommandMode = true;

                ArrayList<Command> commands = new ArrayList<>();
                StateNode iterator = bestEnd;
                while (iterator != null) {
                    if (iterator.lastCommand != null) {
                        commands.add(0, iterator.lastCommand);
                    }
                    System.err.println(iterator.lastCommand);
                    iterator = iterator.parent;
                }

                startingState.loadState();
                bestPathRunner = commands.iterator();
                return;
            }

            if (turnsLoaded >= 300 && curTurn == null) {
                System.err
                        .println("should go into partial rerun " + bestTurn + " " + bestTurn.startingState.saveState.turn);
                runPartialMode = true;
                turnsLoaded = 0;

                ArrayList<Command> commands = new ArrayList<>();
                StateNode iterator = bestTurn.startingState;
                while (iterator != root) {
                    if (iterator.lastCommand != null) {
                        commands.add(0, iterator.lastCommand);
                    }
                    System.err.println(iterator.lastCommand);
                    iterator = iterator.parent;
                }

                root.saveState.loadState();
                bestPathRunner = commands.iterator();
                return;
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

            while (!turns.isEmpty() && (curTurn == null || curTurn.isDone)) {
                curTurn = turns.peek();
                System.err.println("the best turn has damage " + curTurn + " " + turns
                        .size() + " " + (++turnsLoaded));
                if (curTurn.isDone) {
                    System.err.println("finished turn");
                    turns.poll();
                }
            }

            if (curTurn.isDone && turns.isEmpty()) {
                runCommandMode = true;

                ArrayList<Command> commands = new ArrayList<>();
                StateNode iterator = bestEnd;
                while (iterator != null) {
                    commands.add(0, iterator.lastCommand);
                    iterator = iterator.parent;
                }

                startingState.loadState();
                bestPathRunner = commands.iterator();
                return;
            } else {
                boolean reachedNewTurn = curTurn.step();
                if (reachedNewTurn) {
                    curTurn = null;
                }
            }

        }
        if (runPartialMode) {
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

            if (!bestPathRunner.hasNext()) {
                turns = new PriorityQueue<>();
                StateNode rootClone = new StateNode(bestTurn.startingState.parent, bestTurn.startingState.lastCommand, this);
                rootClone.saveState = bestTurn.startingState.saveState;
                root = rootClone;
                TurnNode turnNode = new TurnNode(rootClone, this);
                turns.add(turnNode);

                System.err.println("Done running partial rerun will start from " + turnNode);
                runPartialMode = false;
                curTurn = null;
                bestTurn = null;
            }
        } else if (runCommandMode && shouldRunWhenFound) {
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
