package battleai;

import communicationmod.CommunicationMod;
import savestate.SaveState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;
import java.util.stream.Collectors;

public class BattleAiController {
    public static int minDamage = 5000;
    public static int startingHealth;
    public static Command lastCommand = null;
    private SaveState startingState;
    private Stack<StateNode> states;
    private boolean initialized = false;
    private String bestPath = "";
    public boolean runCommandMode = false;
    private Iterator<Command> bestPathRunner;

    public BattleAiController(SaveState state) {
        minDamage = 5000;
        lastCommand = null;
        startingState = state;
    }

    public BattleAiController(Collection<Command> commands) {
        runCommandMode = true;
        bestPathRunner = commands.iterator();
    }

    public void step() {
        if (!runCommandMode) {
            if (states != null) {
//                String statesString = states.stream().map(StateNode::getStateString)
//                                            .collect(Collectors.joining("-"));
//                System.out.println(statesString);
            }

            if (!initialized) {
                initialized = true;
                runCommandMode = false;
                states = new Stack<>();
                StateNode firstStateContainer = new StateNode(null);
                startingState.loadState();
                startingHealth = startingState.getPlayerHealth();
                states.push(firstStateContainer);
            }

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
                        System.out.printf("replacing best path:%s\n", bestPath);
                        System.out
                                .println("updated bestPathRunner: %s: " + bestPathRunner.hasNext());

                        minDamage = stateDamage;
                    }
//                minDamage = Math.min(minDamage, stateDamage);
                    states.pop();
                    if (!states.isEmpty()) {
                        states.peek().saveState.loadState();
                    }
                } else {
                    states.push(new StateNode(lastCommand));
                }
            }


            if (states.isEmpty()) {
                runCommandMode = true;
                System.out.println("best path is " + bestPath);
            }
        }
        if (runCommandMode) {
            boolean foundCommand = false;
            System.out.printf("%s %s\n", bestPathRunner.hasNext(), foundCommand);
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
        }
    }
}
