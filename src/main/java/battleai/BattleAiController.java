package battleai;

import savestate.SaveState;

import java.util.Stack;
import java.util.stream.Collectors;

public class BattleAiController {
    public static int minDamage = 5000;
    public static int startingHealth;
    public static Command lastCommand = null;
    private final SaveState startingState;
    private Stack<StateNode> states;
    private boolean initialized = false;
    private String bestPath = "";

    public BattleAiController(SaveState state) {
        startingState = state;
    }

    public void step() {
        if (!initialized) {
            initialized = true;
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
                    bestPath = states.stream().map(StateNode::getLastCommand)
                                     .collect(Collectors.joining("\n"));
                    System.out.printf("replacing best path:%s\n", bestPath);

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

        if(states.isEmpty()) {
            System.out.println("best path is " + bestPath);
        }
    }
}
