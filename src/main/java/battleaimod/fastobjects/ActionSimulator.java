package battleaimod.fastobjects;

import battleaimod.BattleAiMod;
import battleaimod.fastobjects.actions.DrawCardActionFast;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.EnableEndTurnButtonAction;
import com.megacrit.cardcrawl.actions.defect.TriggerEndOfTurnOrbsAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.daily.mods.Careless;
import com.megacrit.cardcrawl.daily.mods.ControlledChaos;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.input.DevInputActionSet;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.UnceasingTop;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.util.Iterator;

import static battleaimod.patches.FastActionsPatch.runAndProfile;
import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;

/**
 * This contains static methods that are optimized versions of methods from GameActionAManager,
 * actions can use this variant to update faster
 */
public class ActionSimulator {
    public static void callEndOfTurnActions() {
        long localManagerCallEOT = System.currentTimeMillis();

        AbstractDungeon.getCurrRoom().applyEndOfTurnRelics();
        AbstractDungeon.getCurrRoom().applyEndOfTurnPreCardPowers();
        actionManager.addToBottom(new TriggerEndOfTurnOrbsAction());
        Iterator var1 = AbstractDungeon.player.hand.group.iterator();

        while (var1.hasNext()) {
            AbstractCard c = (AbstractCard) var1.next();
            c.triggerOnEndOfTurnForPlayingCard();
        }

        AbstractDungeon.player.stance.onEndOfTurn();

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController.addRuntime("Local Manager Call EOT", System
                    .currentTimeMillis() - localManagerCallEOT);
        }
    }

    public static void ActionManagerNextAction() {
        if (!actionManager.actions.isEmpty()) {
            actionManager.currentAction = actionManager.actions.remove(0);
            actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;
            actionManager.hasControl = true;
        } else if (!actionManager.preTurnActions.isEmpty()) {
            actionManager.currentAction = actionManager.preTurnActions.remove(0);
            actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;
            actionManager.hasControl = true;
        } else if (!actionManager.cardQueue.isEmpty()) {
            long startCardStuff = System.currentTimeMillis();

            actionManager.usingCard = true;
            CardQueueItem queueItem = actionManager.cardQueue.get(0);
            AbstractCard c = queueItem.card;
            if (c == null) {
                callEndOfTurnActions();
            } else if (c.equals(actionManager.lastCard)) {
                actionManager.lastCard = null;
            }

            if (actionManager.cardQueue.size() == 1 && queueItem.isEndTurnAutoPlay) {
                AbstractRelic top = AbstractDungeon.player.getRelic("Unceasing Top");
                if (top != null) {
                    ((UnceasingTop) top).disableUntilTurnEnds();
                }
            }

            boolean canPlayCard = false;
            if (c != null) {
                c.isInAutoplay = queueItem.autoplayCard;
            }

            if (c != null && actionManager.cardQueue.get(0).randomTarget) {
                queueItem.monster = AbstractDungeon.getMonsters()
                                                   .getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
            }

            if (queueItem.card == null || !c
                    .canUse(AbstractDungeon.player, queueItem.monster) && !queueItem.card.dontTriggerOnUseCard) {
                AbstractDungeon.player.limbo.clear();
            } else {
                canPlayCard = true;
                if (c.freeToPlay()) {
                    c.freeToPlayOnce = true;
                }

                queueItem.card.energyOnUse = queueItem.energyOnUse;
                if (c.isInAutoplay) {
                    queueItem.card.ignoreEnergyOnUse = true;
                } else {
                    queueItem.card.ignoreEnergyOnUse = queueItem.ignoreEnergyTotal;
                }

                if (!queueItem.card.dontTriggerOnUseCard) {
                    AbstractDungeon.player.powers
                            .forEach(power -> power.onPlayCard(queueItem.card, queueItem.monster));

                    AbstractDungeon.getMonsters().monsters.stream()
                                                          .flatMap(monstes -> monstes.powers
                                                                  .stream()).forEach(power -> power
                            .onPlayCard(queueItem.card, queueItem.monster));

                    AbstractDungeon.player.relics
                            .forEach(relic -> relic.onPlayCard(queueItem.card, queueItem.monster));

                    AbstractDungeon.player.stance.onPlayCard(queueItem.card);
                    AbstractDungeon.player.blights.forEach(blight -> blight
                            .onPlayCard(queueItem.card, queueItem.monster));
                    AbstractDungeon.player.hand.group.forEach(triggerCard -> triggerCard
                            .onPlayCard(queueItem.card, queueItem.monster));
                    AbstractDungeon.player.discardPile.group.forEach(triggerCard -> triggerCard
                            .onPlayCard(queueItem.card, queueItem.monster));
                    AbstractDungeon.player.drawPile.group.forEach(triggerCard -> triggerCard
                            .onPlayCard(queueItem.card, queueItem.monster));

                    ++AbstractDungeon.player.cardsPlayedThisTurn;
                    actionManager.cardsPlayedThisTurn.add(queueItem.card);
                    actionManager.cardsPlayedThisCombat.add(queueItem.card);
                }

                if (queueItem.card != null) {
                    if (queueItem.card.target != AbstractCard.CardTarget.ENEMY || queueItem.monster != null && !queueItem.monster
                            .isDeadOrEscaped()) {
                        AbstractDungeon.player
                                .useCard(queueItem.card, queueItem.monster, queueItem.energyOnUse);
                    } else {
                        AbstractDungeon.player.limbo.group.clear();
                    }
                }
            }

            actionManager.cardQueue.remove(0);
            if (!canPlayCard && c != null && c.isInAutoplay) {
                c.dontTriggerOnUseCard = true;
                actionManager.addToBottom(new UseCardAction(c));
            }

            if (BattleAiMod.battleAiController != null) {
                BattleAiMod.battleAiController.addRuntime("Local Action Manager Card Stuff", System
                        .currentTimeMillis() - startCardStuff);
            }

        } else if (!actionManager.monsterAttacksQueued) {
            runAndProfile("Local Action Manager Queue Monster Turn", () -> {
                actionManager.monsterAttacksQueued = true;
                if (!AbstractDungeon.getCurrRoom().skipMonsterTurn) {
                    AbstractDungeon.getCurrRoom().monsters.queueMonsters();
                }
            });
        } else if (!actionManager.monsterQueue.isEmpty()) {
            runAndProfile("Monster Turn", () -> {
                AbstractMonster m = actionManager.monsterQueue.get(0).monster;
                if (!m.isDeadOrEscaped() || m.halfDead) {
                    m.takeTurn();
                    m.applyTurnPowers();
                }
                actionManager.monsterQueue.remove(0);
            });
        } else if (actionManager.turnHasEnded && !AbstractDungeon.getMonsters()
                                                                 .areMonstersBasicallyDead()) {
            if (!AbstractDungeon.getCurrRoom().skipMonsterTurn) {
                AbstractDungeon.getCurrRoom().monsters.applyEndOfTurnPowers();
            }

            AbstractDungeon.player.cardsPlayedThisTurn = 0;
            actionManager.orbsChanneledThisTurn.clear();
            if (ModHelper.isModEnabled("Careless")) {
                Careless.modAction();
            }

            if (ModHelper.isModEnabled("ControlledChaos")) {
                ControlledChaos.modAction();
                AbstractDungeon.player.hand.applyPowers();
            }

            AbstractDungeon.player.applyStartOfTurnRelics();
            AbstractDungeon.player.applyStartOfTurnPreDrawCards();
            AbstractDungeon.player.applyStartOfTurnCards();
            AbstractDungeon.player.applyStartOfTurnPowers();
            AbstractDungeon.player.applyStartOfTurnOrbs();
            ++GameActionManager.turn;
            AbstractDungeon.getCurrRoom().skipMonsterTurn = false;
            actionManager.turnHasEnded = false;
            GameActionManager.totalDiscardedThisTurn = 0;
            actionManager.cardsPlayedThisTurn.clear();
            GameActionManager.damageReceivedThisTurn = 0;
            if (!AbstractDungeon.player.hasPower("Barricade") && !AbstractDungeon.player
                    .hasPower("Blur")) {
                if (!AbstractDungeon.player.hasRelic("Calipers")) {
                    AbstractDungeon.player.loseBlock();
                } else {
                    AbstractDungeon.player.loseBlock(15);
                }
            }

            if (!AbstractDungeon.getCurrRoom().isBattleOver) {
                actionManager
                        .addToBottom(new DrawCardAction(null, AbstractDungeon.player.gameHandSize, true));
                AbstractDungeon.player.applyStartOfTurnPostDrawRelics();
                AbstractDungeon.player.applyStartOfTurnPostDrawPowers();
                actionManager.addToBottom(new EnableEndTurnButtonAction());
            }
        }
    }

    public static void ActionManageUpdate() {
        long localManagerUpdateStart = System.currentTimeMillis();
        switch (actionManager.phase) {
            case WAITING_ON_USER:
                ActionSimulator.ActionManagerNextAction();
                break;
            case EXECUTING_ACTIONS:
                if (actionManager.currentAction != null && !actionManager.currentAction.isDone) {
                    if (actionManager.currentAction instanceof DrawCardAction) {
                        actionManager.currentAction = new DrawCardActionFast(AbstractDungeon.player, actionManager.currentAction.amount);
                    }
                    actionManager.currentAction.update();
                } else {
                    actionManager.previousAction = actionManager.currentAction;
                    actionManager.currentAction = null;
                    ActionSimulator.ActionManagerNextAction();
                    if (actionManager.currentAction == null && AbstractDungeon
                            .getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && !actionManager.usingCard) {
                        actionManager.phase = GameActionManager.Phase.WAITING_ON_USER;
                        AbstractDungeon.player.hand.refreshHandLayout();
                        actionManager.hasControl = false;
                    }

                    actionManager.usingCard = false;
                }
                break;
            default:
        }

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController.addRuntime("Local Manager Update", System
                    .currentTimeMillis() - localManagerUpdateStart);
        }

    }

    public static void roomUpdate() {
        AbstractDungeon.getCurrRoom().monsters.update();
        if (!(AbstractDungeon.getCurrRoom().waitTimer > 0.0F)) {
            if (Settings.isDebug && DevInputActionSet.drawCard.isJustPressed()) {
                actionManager
                        .addToTop(new DrawCardAction(AbstractDungeon.player, 1));
            }

            if (!AbstractDungeon.isScreenUp) {
                ActionSimulator.ActionManageUpdate();
                if (!AbstractDungeon.getCurrRoom().monsters
                        .areMonstersBasicallyDead() && AbstractDungeon.player.currentHealth > 0) {
                    if (AbstractDungeon.player.endTurnQueued && AbstractDungeon.actionManager.cardQueue
                            .isEmpty() && !AbstractDungeon.actionManager.hasControl) {
                        AbstractDungeon.player.endTurnQueued = false;
                        AbstractDungeon.player.isEndingTurn = true;
                    }
                }
            }

            if (AbstractDungeon.player.isEndingTurn) {
                AbstractDungeon.getCurrRoom().endTurn();
            }

        }
    }
}
