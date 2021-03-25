package battleai;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import communicationmod.CommandExecutor;
import savestate.SaveState;

import java.util.ArrayList;
import java.util.List;

public class StateNode {
    final Command lastCommand;
    SaveState saveState;
    private int minDamage = 5000;
    private ArrayList<Command> commands;
    private boolean initialized = false;
    private int commandIndex = -1;
    private boolean isDone = false;


    public StateNode(Command lastCommand) {
        this.lastCommand = lastCommand;
    }

    /**
     * Does the next step and returns true iff the parent should load state
     */
    public boolean step() {
        populateCommands();
        if (!initialized) {
            saveState = new SaveState();
            initialized = true;
            int damage = BattleAiController.startingHealth - saveState.getPlayerHealth();
            if (shouldLookForPlay() && damage < BattleAiController.minDamage) {
                commandIndex = 0;
            } else {
                System.out
                        .printf("Found terminal state on init: damage this combat:%s; best damage: %s\n", damage, BattleAiController.minDamage);

                if (shouldLookForPlay()) {
                    System.out.println("Terminating for damage");
                }

                minDamage = damage;
                isDone = true;
                return true;
            }
        }

        Command toExecute = commands.get(commandIndex);

        BattleAiController.lastCommand = toExecute;
        commandIndex++;
        isDone = commandIndex >= commands.size();

        System.out.printf("executing:%s isDone:%s", toExecute, isDone);
        toExecute.execute();
        return false;
    }

    private boolean shouldLookForPlay() {
        ArrayList<String> maybes = CommandExecutor.getAvailableCommands();
        return maybes.contains("play") || maybes.contains("end");
    }

    private void populateCommands() {
        commands = new ArrayList<>();
        AbstractPlayer player = AbstractDungeon.player;
        List<AbstractCard> hand = player.hand.group;

        List<AbstractMonster> monsters = AbstractDungeon.currMapNode.room.monsters.monsters;

        for (AbstractCard card : hand) {
            if (card.target == AbstractCard.CardTarget.ENEMY || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY) {
                for (AbstractMonster monster : monsters) {
                    if (card.canUse(player, monster)) {
                        commands.add(new CardCommand(card, monster));
//                        System.out.printf("Can use %s on %s\n", card, monster);
                    }
                }
            }

            if (card.target == AbstractCard.CardTarget.SELF || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY) {
                if (card.canUse(player, null)) {
                    commands.add(new CardCommand(card, null));
//                    System.out.printf("Can use %s on self\n", card);
                }
            }
        }

        if (CommandExecutor.isEndCommandAvailable()) {
            commands.add(new EndCommand());
        }
    }

    public boolean isDone() {
        return isDone;
    }

    public int getMinDamage() {
        return minDamage;
    }

    public String getLastCommand() {
        return lastCommand == null ? "start" : lastCommand.toString();
    }
}
