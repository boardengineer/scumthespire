package battleaimod.battleai;

import battleaimod.battleai.playorder.IronCladPlayOrder;
import battleaimod.battleai.playorder.SilentPlayOrder;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import ludicousspeed.simulator.commands.Command;
import ludicousspeed.simulator.commands.CommandList;
import savestate.SaveState;
import savestate.powers.PowerState;

import java.util.List;

import static battleaimod.battleai.TurnNode.getPotionScore;
import static battleaimod.battleai.TurnNode.getTotalMonsterHealth;

public class StateNode {
    private final BattleAiController controller;
    public final StateNode parent;
    final Command lastCommand;
    public String stateString;

    SaveState saveState;
    private int minDamage = 5000;
    private List<Command> commands;
    private boolean initialized = false;
    private int commandIndex = -1;
    private boolean isDone = false;

    public StateNode(StateNode parent, Command lastCommand, BattleAiController controller) {
        this.parent = parent;
        this.lastCommand = lastCommand;
        this.controller = controller;
    }

    /**
     * Does the next step and returns true iff the parent should load state
     */
    public Command step() {
        if (saveState == null) {
            saveState = new SaveState();
        }

        if (commands == null) {
            populateCommands();
        }

        if (!initialized) {
            initialized = true;

            if (AbstractDungeon.player.isDead || AbstractDungeon.player.isDying) {
                controller.deathNode = this;
                isDone = true;
                return null;
            }

            int damage = controller.startingHealth - saveState.getPlayerHealth();

            boolean isBattleWon = isBattleOver();
            if (!isBattleWon && damage < (controller.minDamage + 6)) {
                commandIndex = 0;
            } else {
//                System.err
//                        .printf("Found terminal state on init: damage this combat:%s; best damage: %s\n", damage, controller.minDamage);

                if (isBattleWon) {
                    if (controller.bestEnd == null || (getStateScore(this) > getStateScore(controller.bestEnd))
                            && saveState.getPlayerHealth() >= 1) {
                        controller.minDamage = damage;
                        controller.bestEnd = this;
                    }
                } else if (AbstractDungeon.player.isDead || AbstractDungeon.player.isDying) {
                    controller.deathNode = this;
                }

                minDamage = damage;
                isDone = true;
                return null;
            }
        }

        if (commands.size() == 0) {
            isDone = true;
            return null;
        }

        Command toExecute = commands.get(commandIndex);
        commandIndex++;
        isDone = commandIndex >= commands.size();

        return toExecute;
    }

    private boolean isBattleOver() {
        return AbstractDungeon.getCurrRoom().monsters.areMonstersBasicallyDead();
    }

    private void populateCommands() {
        commands = CommandList.getAvailableCommands((card1, card2) -> {
            if (IronCladPlayOrder.CARD_RANKS.containsKey(card1
                    .cardID) && IronCladPlayOrder.CARD_RANKS
                    .containsKey(card2.cardID)) {
                return IronCladPlayOrder.CARD_RANKS
                        .get(card1.cardID) - IronCladPlayOrder.CARD_RANKS
                        .get(card2.cardID);
            } else if (SilentPlayOrder.CARD_RANKS.containsKey(card1
                    .cardID) && SilentPlayOrder.CARD_RANKS
                    .containsKey(card2.cardID)) {
                return SilentPlayOrder.CARD_RANKS
                        .get(card1.cardID) - SilentPlayOrder.CARD_RANKS
                        .get(card2.cardID);
            }

            return card2.costForTurn - card1.costForTurn;
        });
    }

    public boolean isDone() {
        return isDone;
    }

    public int getPlayerHealth() {
        return saveState.getPlayerHealth();
    }

    public static int getPlayerDamage(StateNode node) {
        return node.controller.startingHealth - node.saveState.getPlayerHealth();
    }

    public static int getStateScore(StateNode node) {
        int playerDamage = getPlayerDamage(node);
        int monsterDamage = getTotalMonsterHealth(node.controller.startingState) - getTotalMonsterHealth(node.saveState);

        int strength = 0;
        int dexterity = 0;
        for (PowerState power : node.saveState.playerState.powers) {
            if (power.powerId.equals("Strength")) {
                strength = power.amount;
            } else if (power.powerId.equals("Dexterity")) {
                dexterity = power.amount;
            }
        }

        int potionLoss = getPotionScore(node.controller.startingState) - getPotionScore(node.saveState);

        return monsterDamage - 8 * playerDamage + 3 * strength + 3 * dexterity - potionLoss;
    }
}
