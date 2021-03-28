package battleai;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import communicationmod.CommandExecutor;
import savestate.SaveState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                System.err
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
        Set<String> seenCommands = new HashSet<>();

        for (int i = 0; i < hand.size(); i++) {
            AbstractCard card = hand.get(i);

            String setName = card.name + (card.upgraded ? "+" : "");
            int oldCount = seenCommands.size();
            seenCommands.add(setName);
            if (oldCount == seenCommands.size()) {
                continue;
            }

            if (card.target == AbstractCard.CardTarget.ENEMY || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY) {
                for (int j = 0; j < monsters.size(); j++) {
                    AbstractMonster monster = monsters.get(j);
                    if (card.canUse(player, monster)) {
                        commands.add(0, new CardCommand(i, j));
                    }
                }
            }

            if (card.target == AbstractCard.CardTarget.SELF || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY) {
                if (card.canUse(player, null)) {
                    commands.add(new CardCommand(i));
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

    public String getLastCommandString() {
        return lastCommand == null ? "start" : lastCommand.toString();
    }

    public Command getLastCommand() {
        return lastCommand;
    }

    public int getPlayerHealth() {
        return saveState.getPlayerHealth();
    }

    public String getHandString() {
        return saveState.getPlayerHand();
    }

    public String getStateString() {
        return String.format(" %2d / %2d ", commandIndex, commands != null ? commands.size() : 0);
    }

}
