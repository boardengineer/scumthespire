package battleaimod.battleai;

import battleaimod.savestate.SaveState;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.util.*;
import java.util.stream.Collectors;

public class StateNode {
    private final BattleAiController controller;
    public final StateNode parent;
    final Command lastCommand;
    public String stateString;

    SaveState saveState;
    private int minDamage = 5000;
    private ArrayList<Command> commands;
    private boolean initialized = false;
    private int commandIndex = -1;
    private boolean isDone = false;

    public StateNode(StateNode parent, Command lastCommand, BattleAiController controller) {
        this.parent = parent;
        this.lastCommand = lastCommand;
        this.controller = controller;
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
    public Command step() {
        if (saveState == null) {
            saveState = new SaveState();
        }

        if (commands == null) {
            populateCommands();
        }

        if (!initialized) {
            initialized = true;

            // This will be a problem when we win on the enemy turn
            // Start of turn
            if (lastCommand == null || lastCommand instanceof EndCommand) {
//                if (saveToParent()) {
//                    System.err.println("deduping turn");
//                    return null;
//                }
            }

            int damage = controller.startingHealth - saveState.getPlayerHealth();

            boolean isBattleOver = !shouldLookForPlay();
            if (!isBattleOver && damage < (controller.minDamage + 6)) {
                commandIndex = 0;
            } else {
                System.err
                        .printf("Found terminal state on init: damage this combat:%s; best damage: %s\n", damage, controller.minDamage);

                if (isBattleOver) {
                    if (damage < controller.minDamage && saveState.getPlayerHealth() >= 1) {
                        controller.minDamage = damage;
                        controller.bestEnd = this;
                    }
                }

                minDamage = damage;
                isDone = true;
                return null;
            }
        }

        Command toExecute = commands.get(commandIndex);
        commandIndex++;
        isDone = commandIndex >= commands.size();

        return toExecute;
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
                        commands.add(0, new CardCommand(i, j, card.cardID));
                    }
                }
            }

            if (card.target == AbstractCard.CardTarget.ALL_ENEMY || card.target == AbstractCard.CardTarget.ALL) {
                if (card.canUse(player, null)) {
                    commands.add(0, new CardCommand(i, card.cardID));
                }
            }

            if (card.target == AbstractCard.CardTarget.SELF || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY) {
                if (card.canUse(player, null)) {
                    commands.add(new CardCommand(i, card.cardID));
                }
            }


        }

        if (isEndCommandAvailable()) {
            commands.add(new EndCommand(saveState));
        }

        commands.sort(new Comparator<Command>() {
            @Override
            public int compare(Command o1, Command o2) {
                if (o2 instanceof EndCommand) {
                    return -1;
                } else if (o1 instanceof EndCommand) {
                    return 1;
                }

                CardCommand cardCommand1 = (CardCommand) o1;
                CardCommand cardCommand2 = (CardCommand) o2;

                AbstractCard card1 = CardLibrary.getCard(cardCommand1.displayString);
                AbstractCard card2 = CardLibrary.getCard(cardCommand2.displayString);

                if (card1.cost == 0) {
                    return -1;
                } else if (card2.cost == 0) {
                    return 1;
                }

                if (card1.type == AbstractCard.CardType.POWER) {
                    if (card2.type == AbstractCard.CardType.POWER) {
                        return card2.cost - card1.cost;
                    }
                    return -1;
                } else if (card2.type == AbstractCard.CardType.POWER) {
                    return 1;
                }

                return card2.cost - card1.cost;
            }
        });
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

    public String getTurnString() {
        ArrayList<String> commandsThisTurn = new ArrayList<>();
        StateNode iterator = parent == null ? this : parent;
        while ((iterator.lastCommand != null) && !(iterator.lastCommand instanceof EndCommand)) {
            commandsThisTurn.add(iterator.lastCommand.toString());
            iterator = iterator.parent;
        }
        return commandsThisTurn.stream().sorted().collect(Collectors.joining());
    }
}
