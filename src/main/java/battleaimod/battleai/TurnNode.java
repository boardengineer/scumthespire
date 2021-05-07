package battleaimod.battleai;

import battleaimod.BattleAiMod;
import battleaimod.battleai.commands.Command;
import battleaimod.battleai.commands.EndCommand;
import battleaimod.battleai.commands.StateDebugInfo;
import battleaimod.savestate.PotionState;
import battleaimod.savestate.powers.PowerState;
import battleaimod.savestate.SaveState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private Optional<Integer> cachedValue = Optional.empty();

    static int nodeIndex = 0;


    public TurnNode(StateNode statenode, BattleAiController controller, TurnNode parent) {
        startingState = statenode;
        this.controller = controller;
        this.parent = parent;
        this.turnLabel = nodeIndex++;
        children = new ArrayList<>();

//        if (parent != null) {
//            parent.children.add(this);
//        }
    }

    public boolean step() {
        long stepStart = System.currentTimeMillis();

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
            controller.addRuntime("Battle AI TurnStep Checkpoint 0", System
                    .currentTimeMillis() - stepStart);
            return false;
        }


        if (curState.saveState == null) {
            curState.saveState = new SaveState();
        }

        if (curState.getPlayerHealth() < 1) {
            controller.deathNode = curState;
            isDone = true;
            return true;
        }

        controller.addRuntime("Battle AI TurnStep Checkpoint 1", System
                .currentTimeMillis() - stepStart);

        if (curState != startingState && isNewTurn(curState)) {
            controller.turnsLoaded++;
            controller.addRuntime("turnsLoaded", 1);
            TurnNode toAdd = new TurnNode(curState, controller, this);
            states.pop();

            while (!states.isEmpty() && states.peek().isDone()) {
                states.pop();
            }
            controller.addRuntime("Battle AI TurnStep early Return Total 1", System
                    .currentTimeMillis() - stepStart);

            runningCommands = false;
            if (curState.lastCommand instanceof EndCommand) {
                ((EndCommand) curState.lastCommand).stateDebugInfo = new StateDebugInfo(curState.saveState);
            }

//            if (controller.bestTurn == null || controller.bestTurn.startingState.saveState.turn < curState.saveState.turn || (controller.bestTurn.startingState.saveState.turn == curState.saveState.turn && this
//                    .isBetterThan(controller.bestTurn))) {
//                controller.bestTurn = this;
//            }

            controller.addRuntime("Battle AI TurnStep early Return Total 2", System
                    .currentTimeMillis() - stepStart);

            int turnNumber = curState.saveState.turn;
            if (curState.saveState.getPlayerHealth() >= 1) {
                if (turnNumber >= controller.targetTurn) {
                    if (controller.bestTurn == null || toAdd.isBetterThan(controller.bestTurn)) {
                        controller.bestTurn = toAdd;
                    }

                } else {
                    if (controller.backupTurn == null ||
                            controller.backupTurn.startingState.saveState.turn < toAdd.startingState.saveState.turn ||
                            (toAdd.isBetterThan(controller.backupTurn)) && controller.backupTurn.startingState.saveState.turn == toAdd.startingState.saveState.turn) {
                        controller.backupTurn = toAdd;
                    }
//                    System.err.println("adding " + toAdd);

                    long startAdd = System.currentTimeMillis();

                    controller.turns.add(toAdd);

                    controller.addRuntime("Battle AI TurnStep Add Turn", System
                            .currentTimeMillis() - startAdd);
                }
            }

            turnIndex++;

            BattleAiMod.readyForUpdate = true;
            controller.addRuntime("Battle AI TurnStep early Return Total", System
                    .currentTimeMillis() - stepStart);

            return true;
        }
        controller.addRuntime("Battle AI TurnStep Checkpoint 2", System
                .currentTimeMillis() - stepStart);

        if (curState.isDone()) {
            states.pop();
            if (!states.empty()) {
                states.peek().saveState.loadState();
            }
        } else {
            long startNodeSep = System.currentTimeMillis();

            Command toExecute = curState.step();

            controller.addRuntime("Battle AI StateNode Step", System
                    .currentTimeMillis() - startNodeSep);

            if (toExecute == null) {
                states.pop();
                if (!states.isEmpty()) {
                    states.peek().saveState.loadState();
                }
                BattleAiMod.readyForUpdate = true;
            } else {
//                System.err.println("adding node for " + toExecute);
                StateNode toAdd = new StateNode(curState, toExecute, controller);
                states.push(toAdd);

                long startExecute = System.currentTimeMillis();

                toExecute.execute();

                controller.addRuntime("Battle AI Execute Action", System
                        .currentTimeMillis() - startExecute);
            }
        }

        controller.addRuntime("Battle AI TurnStep Checkpoint 3", System
                .currentTimeMillis() - stepStart);

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
        return StateNode.getPlayerDamage(turnNode.startingState);
    }

    public static int getNumSlimes(TurnNode turnNode) {
        return turnNode.startingState.saveState.getNumSlimes();
    }

    public static int getPotionScore(SaveState saveState) {
        return saveState.playerState.potions.stream().map(potionState -> {
            if (potionState.potionId.equals("Potion Slot") || !PotionState.POTION_VALUES
                    .containsKey(potionState.potionId)) {
                return 0;
            }
            return PotionState.POTION_VALUES.get(potionState.potionId);
        }).reduce(Integer::sum).get();
    }

    public static int getTurnScore(TurnNode turnNode) {
        if (!turnNode.cachedValue.isPresent()) {
            turnNode.cachedValue = Optional.of(caclculateTurnScore(turnNode));
        }
        return turnNode.cachedValue.get();
    }

    public static int getTotalMonsterHealth(SaveState saveState) {
        return saveState.curMapNodeState.monsterData.stream()
                                                    .map(monster -> {
                                                        if (monster.powers.stream()
                                                                          .filter(power -> power.powerId
                                                                                  .equals("Barricade"))
                                                                          .findAny().isPresent()) {
                                                            return monster.currentHealth + monster.currentBlock;
                                                        } else if (monster.powers.stream()
                                                                                 .filter(power -> power.powerId
                                                                                         .equals("Unawakened"))
                                                                                 .findAny()
                                                                                 .isPresent()) {
                                                            return monster.currentHealth + monster.maxHealth;
                                                        }
                                                        return monster.currentHealth;
                                                    })
                                                    .reduce(Integer::sum)
                                                    .get();
    }

    public static int caclculateTurnScore(TurnNode turnNode) {
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

        int potionLoss = getPotionScore(turnNode.controller.startingState) - getPotionScore(turnNode.startingState.saveState);

        return monsterDamage - 8 * playerDamage + 3 * strength + 3 * dexterity - potionLoss;
    }

    @Override
    public int compareTo(TurnNode otherTurn) {
        long startCompare = System.currentTimeMillis();

        int result = getTurnScore(otherTurn) - getTurnScore(this);

        controller.addRuntime("Comparing Turns", System.currentTimeMillis() - startCompare);
        controller.addRuntime("Comparing Turns instance", 1);

        return result;
    }

    public boolean isBetterThan(TurnNode other) {
        return compareTo(other) < 0;
    }

    private boolean isNewTurn(StateNode childNode) {
        return (childNode.saveState.turn > this.startingState.saveState.turn) || childNode.lastCommand instanceof EndCommand;
    }
}
