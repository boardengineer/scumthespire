package battleai;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import savestate.SaveState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StateNode {
    final StateNode parent;
    final Command lastCommand;
    SaveState saveState;
    private int minDamage = 5000;
    private ArrayList<Command> commands;
    private HashSet<String> turns = new HashSet<>();
    private boolean initialized = false;
    private int commandIndex = -1;
    private boolean isDone = false;

    public StateNode(StateNode parent, Command lastCommand) {
        this.parent = parent;
        this.lastCommand = lastCommand;
    }

    private static boolean isInDungeon() {
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

    /**
     * Does the next step and returns true iff the parent should load state
     */
    public boolean step() {
        populateCommands();

        if (!initialized) {
            saveState = new SaveState();
            String stateString = saveState.getDedupeString();

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

        if (toExecute instanceof EndCommand) {
            StateNode iterator = this;
            ArrayList<String> commandsThisTurn = new ArrayList<>();
            while ((iterator.lastCommand != null) && !(iterator.lastCommand instanceof EndCommand)) {
                commandsThisTurn.add(iterator.lastCommand.toString());
                iterator = iterator.parent;
            }

            String turnString = commandsThisTurn.stream().sorted().collect(Collectors.joining());
            if (iterator.turns.contains(turnString)) {
                System.err.println("deduping turn");
                return true;
            }
            iterator.turns.add(turnString);
        }

        toExecute.execute();
        return false;
    }

    private boolean shouldLookForPlay() {
        return shouldCheckForPlays() || isEndCommandAvailable();
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
                        commands.add(0, new CardCommand(i, j, card.name));
                    }
                }
            }

            if (card.target == AbstractCard.CardTarget.SELF || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY) {
                if (card.canUse(player, null)) {
                    commands.add(new CardCommand(i, card.name));
                }
            }
        }

        if (isEndCommandAvailable()) {
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
