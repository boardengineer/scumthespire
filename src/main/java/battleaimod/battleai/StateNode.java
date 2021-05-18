package battleaimod.battleai;

import basemod.ReflectionHacks;
import battleaimod.simulator.commands.*;
import battleaimod.battleai.playorder.IronCladPlayOrder;
import battleaimod.battleai.playorder.SilentPlayOrder;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.TwinStrike;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.PotionSlot;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import com.megacrit.cardcrawl.ui.buttons.CardSelectConfirmButton;
import savestate.PotionState;
import savestate.SaveState;
import savestate.powers.PowerState;

import java.util.*;

import static battleaimod.battleai.TurnNode.getPotionScore;
import static battleaimod.battleai.TurnNode.getTotalMonsterHealth;

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
        return isInDungeon() &&
                (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT &&
                        !AbstractDungeon.isScreenUp &&
                        (AbstractDungeon.actionManager.currentAction == null && AbstractDungeon.actionManager.actions
                                .isEmpty()));
    }

    public static boolean isInHandSelect() {
        return isInDungeon() &&
                AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT &&
                AbstractDungeon.isScreenUp &&
                AbstractDungeon.screen == AbstractDungeon.CurrentScreen.HAND_SELECT;
    }

    public static boolean isInGridSelect() {
        return isInDungeon() &&
                AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT &&
                AbstractDungeon.isScreenUp &&
                AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID;
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
        commands = new ArrayList<>();
        AbstractPlayer player = AbstractDungeon.player;
        List<AbstractCard> hand = player.hand.group;
        List<AbstractPotion> potions = player.potions;

        List<AbstractMonster> monsters = AbstractDungeon.currMapNode.room.monsters.monsters;
        Set<String> seenCommands = new HashSet<>();

        if (shouldCheckForPlays()) {
            HashMap<AbstractCard, Integer> cardIndeces = new HashMap<>();

            for (int i = 0; i < hand.size(); i++) {
                cardIndeces.put(hand.get(i), i);
            }

            cardIndeces.entrySet().stream()
                       .sorted((card1, card2) -> {
                           if (IronCladPlayOrder.CARD_RANKS.containsKey(card1
                                   .getKey().cardID) && IronCladPlayOrder.CARD_RANKS
                                   .containsKey(card2.getKey().cardID)) {
                               return IronCladPlayOrder.CARD_RANKS
                                       .get(card1.getKey().cardID) - IronCladPlayOrder.CARD_RANKS
                                       .get(card2.getKey().cardID);
                           } else if (SilentPlayOrder.CARD_RANKS.containsKey(card1
                                   .getKey().cardID) && SilentPlayOrder.CARD_RANKS
                                   .containsKey(card2.getKey().cardID)) {
                               return SilentPlayOrder.CARD_RANKS
                                       .get(card1.getKey().cardID) - SilentPlayOrder.CARD_RANKS
                                       .get(card2.getKey().cardID);
                           }

                           return card2.getKey().costForTurn - card1.getKey().costForTurn;
                       }).forEach(
                    cardEntry -> {
                        AbstractCard card = cardEntry.getKey();

                        // Only populate the first time you've seen a card with this specific {name X upgraded}
                        String setName = card.name + (card.upgraded ? "+" : "");
                        int oldCount = seenCommands.size();
                        seenCommands.add(setName);
                        if (oldCount == seenCommands.size()) {
                            return;
                        }

                        if (card.target == AbstractCard.CardTarget.ENEMY || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY) {
                            for (int j = 0; j < monsters.size(); j++) {
                                AbstractMonster monster = monsters.get(j);
                                if (card.canUse(player, monster) && !monster.isDeadOrEscaped()) {
                                    commands.add(new CardCommand(cardEntry.getValue(), j, String
                                            .format(card.cardID + " for " + card.costForTurn)));
                                }
                            }
                        }

                        if (card.target == AbstractCard.CardTarget.ALL_ENEMY || card.target == AbstractCard.CardTarget.ALL) {
                            if (card.canUse(player, null)) {
                                commands.add(new CardCommand(cardEntry
                                        .getValue(), card.cardID + " for " + card.baseBlock));
                            }
                        }

                        if (card.target == AbstractCard.CardTarget.SELF || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY || card.target == AbstractCard.CardTarget.NONE) {
                            if (card.canUse(player, null)) {
                                commands.add(new CardCommand(cardEntry
                                        .getValue(), card.cardID + " for " + card.baseMagicNumber));
                            }
                        }

                    }
            );

            for (int i = 0; i < potions.size(); i++) {
                AbstractPotion potion = potions.get(i);
                if (!potion
                        .canUse() || !potion.isObtained || potion instanceof PotionSlot || PotionState.UNPLAYABLE_POTIONS
                        .contains(potion.ID)) {
                    continue;
                }

                // Dedupe potions
                String setName = potion.name;
                int oldCount = seenCommands.size();
                seenCommands.add(setName);
                if (oldCount == seenCommands.size()) {
                    continue;
                }

                TwinStrike twinStrike;

                if (potion.targetRequired) {
                    for (int j = 0; j < monsters.size(); j++) {
                        AbstractMonster monster = monsters.get(j);
                        if (!monster.isDeadOrEscaped()) {
                            commands.add(new PotionCommand(i, j));
                        }
                    }
                } else {
                    commands.add(new PotionCommand(i));
                }
            }
        }

        if (isInHandSelect()) {
            if (AbstractDungeon.handCardSelectScreen.selectedCards.group
                    .size() < AbstractDungeon.handCardSelectScreen.numCardsToSelect) {
                for (int i = 0; i < AbstractDungeon.player.hand.size(); i++) {
                    commands.add(new HandSelectCommand(i));
                }
//                throw new IllegalStateException("blah blah");
            }

            if (isHandSelectConfirmButtonEnabled()) {
                commands.add(HandSelectConfirmCommand.INSTANCE);
            }
        }

        if (isInGridSelect()) {
            for (int i = 0; i < AbstractDungeon.gridSelectScreen.targetGroup.size(); i++) {
                commands.add(new GridSelectCommand(i));
            }
        }

        if (isEndCommandAvailable()) {
            commands.add(new EndCommand());
        }

        GridCardSelectScreen gridSelectScreen = AbstractDungeon.gridSelectScreen;
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

    private static boolean isHandSelectConfirmButtonEnabled() {
        CardSelectConfirmButton button = AbstractDungeon.handCardSelectScreen.button;
        boolean isHidden = ReflectionHacks
                .getPrivate(button, CardSelectConfirmButton.class, "isHidden");
        boolean isDisabled = button.isDisabled;
        return !(isHidden || isDisabled);
    }
}
