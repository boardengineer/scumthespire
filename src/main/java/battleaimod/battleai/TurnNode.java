package battleaimod.battleai;

import battleaimod.BattleAiMod;
import battleaimod.savestate.PowerState;
import battleaimod.savestate.SaveState;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TurnNode implements Comparable<TurnNode> {
    private final BattleAiController controller;
    public final int turnLabel;
    public Stack<StateNode> states;
    public boolean runningCommands = false;
    public boolean isDone = false;

    public StateNode startingState;
    private boolean initialized = false;
    int turnIndex = 0;

    public List<TurnNode> children;
    public TurnNode parent;

    static int nodeIndex = 0;


    public TurnNode(StateNode statenode, BattleAiController controller, TurnNode parent) {
        startingState = statenode;
        this.controller = controller;
        this.parent = parent;
        this.turnLabel = nodeIndex++;
        children = new ArrayList<>();

        if (parent != null) {
            parent.children.add(this);
        }
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
            return false;
        }

        if (curState != startingState && curState.lastCommand instanceof EndCommand) {
            controller.turnsLoaded++;
            TurnNode toAdd = new TurnNode(curState, controller, this);
            states.pop();

            while (!states.isEmpty() && states.peek().isDone()) {
                states.pop();
            }

            runningCommands = false;
            if (curState.saveState == null) {
                curState.saveState = new SaveState();
            }

            ((EndCommand) curState.lastCommand).stateDebugInfo = new StateDebugInfo(curState.saveState);

//            if (controller.bestTurn == null || controller.bestTurn.startingState.saveState.turn < curState.saveState.turn || (controller.bestTurn.startingState.saveState.turn == curState.saveState.turn && this
//                    .isBetterThan(controller.bestTurn))) {
//                controller.bestTurn = this;
//            }

            int turnNumber = curState.saveState.turn;
            if (curState.saveState.getPlayerHealth() >= 1) {
                if (turnNumber >= controller.targetTurn) {
                    if (controller.bestTurn == null || toAdd.isBetterThan(controller.bestTurn)) {
                        controller.bestTurn = toAdd;
                    }

                } else {
                    if (controller.backupTurn == null || (toAdd
                            .isBetterThan(controller.backupTurn)) && controller.backupTurn.startingState.saveState.turn <= toAdd.startingState.saveState.turn) {
                        controller.backupTurn = toAdd;
                    }
                    System.err.println("adding " + toAdd);
                    controller.turns.add(toAdd);
                }
            }

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
//                System.err.println("adding node for " + toExecute);
                StateNode toAdd = new StateNode(curState, toExecute, controller);
                if (toExecute instanceof EndCommand) {
                    states.push(toAdd);
                    toExecute.execute();
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
        int monsterDamage = getTotalMonsterHealth(controller.startingState) - getTotalMonsterHealth(startingState.saveState);

        return String
                .format("hp:%03d ehp:%03d (%03d) turn:%02d score:%4d", getPlayerDamage(this), getTotalMonsterHealth(this), monsterDamage, startingState.saveState.turn, getTurnScore(this));
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


        int strength = 0;
        int dexterity = 0;
        for (PowerState power : turnNode.startingState.saveState.playerState.powers) {
            if (power.powerId.equals("Strength")) {
                strength = power.amount;
            } else if (power.powerId.equals("Dexterity")) {
                dexterity = power.amount;
            }
        }

        // (player damage) / (player starting health) vs (monster damage / monster total health)


        if (turnNode.startingState.saveState.encounterName != null) {
            String encounterName = turnNode.startingState.saveState.encounterName;
            if (encounterName.equals("Lagavulin")) {
                return monsterDamage - playerDamage + 3 * strength + 3 * dexterity;
            } else if (encounterName.equals("Hexaghost")) {
                return monsterDamage - 4 * playerDamage + 3 * strength + 3 * dexterity;
            } else if (encounterName.equals("Champ")) {
                return monsterDamage - 8 * playerDamage + 3 * strength + 3 * dexterity;
            } else if (encounterName.equals("The Guardian")) {
                return monsterDamage - 8 * playerDamage + 3 * strength + 3 * dexterity;
            }
        }

        return monsterDamage - 2 * playerDamage + 3 * strength + 3 * dexterity;
    }

    @Override

    public int compareTo(TurnNode otherTurn) {
        return getTurnScore(otherTurn) - getTurnScore(this);
    }

    public boolean isBetterThan(TurnNode other) {
        return compareTo(other) < 0;
    }
}
