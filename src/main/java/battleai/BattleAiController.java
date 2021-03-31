package battleai;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import communicationmod.ChoiceScreenUtils;
import communicationmod.CommunicationMod;
import org.apache.logging.log4j.LogManager;
import savestate.SaveState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;
import java.util.stream.Collectors;

public class BattleAiController {
    public static StateNode root = null;
    public static int minDamage = 5000;
    public static int startingHealth;
    public static Command lastCommand = null;
    public static Stack<StateNode> states;
    public boolean runCommandMode = false;
    public boolean isDone = false;
    private SaveState startingState;
    private boolean initialized = false;
    private String bestPath = "";
    private Iterator<Command> bestPathRunner;
    private long startingNanos;
    private long lastStepNanos;
    private int steps;

    public BattleAiController(SaveState state) {
        minDamage = 5000;
        lastCommand = null;
        startingState = state;
    }

    public BattleAiController(Collection<Command> commands) {
        runCommandMode = true;
        bestPathRunner = commands.iterator();
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
        if (!runCommandMode) {
            long currentTime = System.nanoTime();
            if (currentTime - lastStepNanos > 500_000_000 || (lastCommand != null && lastCommand instanceof EndCommand) || true) {
                LogManager.getLogger("hello").info(String
                        .format("step time:%s last Command: %s step: %s\n", (currentTime - lastStepNanos) / 1E6, lastCommand == null ? "null" : lastCommand
                                .getClass(), steps++));
            } else {
                steps++;
            }

            if (!initialized) {
                startingNanos = lastStepNanos = System.nanoTime();
                steps = 0;
                StateNode.turnLabel = 0;
                initialized = true;
                runCommandMode = false;
                states = new Stack<>();
                StateNode firstStateContainer = new StateNode(null, null);
                startingState.loadState();
                startingHealth = startingState.getPlayerHealth();
                root = firstStateContainer;
                states.push(firstStateContainer);
            }

            System.err.println(String.format("step: %s depth: %s", steps, states.size()));
            lastStepNanos = currentTime;

            StateNode curState = states.peek();
            if (curState.isDone()) {
                minDamage = Math.min(minDamage, curState.getMinDamage());
                states.pop();
                if (!states.empty()) {
                    states.peek().saveState.loadState();
                }
            } else {
                boolean shouldContinue = curState.step();
                if (shouldContinue) {
                    int stateDamage = curState.getMinDamage();
                    if (stateDamage < minDamage) {
                        bestPath = states.stream().map(node -> String
                                .format("%s %s %s\n", node.getLastCommandString(), node
                                        .getPlayerHealth(), node.getHandString()))
                                         .collect(Collectors.joining("\n"));
                        bestPathRunner = states.stream().map(StateNode::getLastCommand)
                                               .collect(Collectors.toCollection(ArrayList::new))
                                               .iterator();

                        minDamage = stateDamage;
                    }

                    states.pop();
                    if (!states.isEmpty()) {
                        states.peek().saveState.loadState();
                    }
                } else {
                    states.push(new StateNode(curState, lastCommand));
                }
            }


            if (states.isEmpty()) {
                root = null;
                runCommandMode = true;
                System.out.println("best path is " + bestPath);
                System.err.printf("%s %s\n", steps, (System
                        .nanoTime() - startingNanos) / 1E9);
            }
        }
        if (runCommandMode) {
            boolean foundCommand = false;
            while (bestPathRunner.hasNext() && !foundCommand) {
                Command command = bestPathRunner.next();
                if (command != null) {
                    System.out.println("running command " + command);
                    CommunicationMod.readyForUpdate = true;
                    foundCommand = true;
                    command.execute();
                } else {
                    foundCommand = true;
                    startingState.loadState();
                }
            }

            if (!bestPathRunner.hasNext()) {
                isDone = true;
            }
        }
    }
}
