package battleaimod.battleai;

import battleaimod.BattleAiMod;
import battleaimod.savestate.SaveState;

import java.util.HashSet;
import java.util.Stack;

public class TurnNode implements Comparable<TurnNode> {
    private final BattleAiController controller;
    public Stack<StateNode> states;
    private final HashSet<String> completedTurns = new HashSet<>();
    public boolean runningCommands = false;
    public boolean isDone = false;
    public StateNode startingState;
    private boolean initialized = false;
    int turnIndex = 0;

    public TurnNode(StateNode statenode, BattleAiController controller) {
        startingState = statenode;
        this.controller = controller;
    }

    public boolean step() {
        if (isDone) {
            return true;
        }

        if (!initialized) {
            initialized = true;
            states = new Stack<>();
            states.push(startingState);
        }

        if (states.isEmpty()) {
            BattleAiMod.readyForUpdate = true;
            isDone = true;
            return true;
        }

        StateNode curState = states.peek();
        if (!runningCommands) {
            runningCommands = true;
            curState.saveState.loadState();
        }

        if (curState != startingState && curState.lastCommand instanceof EndCommand) {
            TurnNode toAdd = new TurnNode(curState, controller);
            states.pop();
            runningCommands = false;
            if (curState.saveState == null) {
                curState.saveState = new SaveState();
            }

            if (controller.bestTurn == null || controller.bestTurn.startingState.saveState.turn < curState.saveState.turn || (controller.bestTurn.startingState.saveState.turn == curState.saveState.turn && this
                    .isBetterThan(controller.bestTurn))) {
                controller.bestTurn = this;
            }

            controller.turns.add(toAdd);
            turnIndex++;

            BattleAiMod.readyForUpdate = true;
            return true;
        }
        if (curState.isDone()) {
            states.pop();
            if (!states.empty()) {
                states.peek().saveState.loadState();
            }
        } else {
            Command toExecute = curState.step();
            if (toExecute == null) {
                states.pop();
                if (!states.isEmpty()) {
                    states.peek().saveState.loadState();
                }
            } else {
                StateNode toAdd = new StateNode(curState, toExecute, controller);
                if (toExecute instanceof EndCommand) {
                    String dedupeString = toAdd.getTurnString();
                    if (!completedTurns.contains(dedupeString)) {
                        completedTurns.add(dedupeString);
                        states.push(toAdd);
                        toExecute.execute();
                    } else {
                        BattleAiMod.readyForUpdate = true;
                    }
                } else {
                    states.push(toAdd);
                    toExecute.execute();
                }

            }
        }

        if (states.isEmpty()) {
            isDone = true;
        }

        return false;
    }

    @Override
    public String toString() {
        return getPlayerDamage(this) + " " + getTotalMonsterHealth(this) + " " + turnIndex + " " + startingState.saveState.turn + " slimes: " + getNumSlimes(this);
    }

    public static int getTotalMonsterHealth(TurnNode turnNode) {
        return getTotalMonsterHealth(turnNode.startingState.saveState);
    }

    public static int getPlayerDamage(TurnNode turnNode) {
        return turnNode.controller.startingHealth - turnNode.startingState.saveState
                .getPlayerHealth();
    }

    public static int getNumSlimes(TurnNode turnNode) {
        return turnNode.startingState.saveState.getNumSlimes();
    }

    public static int getTotalMonsterHealth(SaveState saveState) {
        return saveState.curMapNodeState.monsterData.stream()
                                               .map(monster -> monster.currentHealth)
                                               .reduce(Integer::sum)
                                               .get();
    }

    public static int getTurnScore(TurnNode turnNode) {
        int playerDamage = getPlayerDamage(turnNode);
        int monsterDamage = getTotalMonsterHealth(turnNode.controller.startingState) - getTotalMonsterHealth(turnNode.startingState.saveState);

        return monsterDamage - 2 * (Math.max(0, playerDamage - 6));
    }

    @Override
    public int compareTo(TurnNode otherTurn) {
        return getTurnScore(otherTurn) - getTurnScore(this);
    }

    public boolean isBetterThan(TurnNode other) {
        return compareTo(other) < 0;
    }
}
