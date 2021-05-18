package battleaimod.simulator;

import battleaimod.BattleAiMod;
import com.megacrit.cardcrawl.actions.ClearCardQueueAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.SetAnimationAction;
import com.megacrit.cardcrawl.actions.common.DiscardAtEndOfTurnAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.EnableEndTurnButtonAction;
import com.megacrit.cardcrawl.actions.common.ShowMoveNameAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.daily.mods.Careless;
import com.megacrit.cardcrawl.daily.mods.ControlledChaos;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.UnceasingTop;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import savestate.actions.EnqueueEndTurnAction;

import java.util.Iterator;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;

/**
 * This contains static methods that are optimized versions of methods from GameActionAManager,
 * actions can use this variant to update faster
 */
public class ActionSimulator {
    /**
     * A blocking loop that's meant to replace the logic from GameActionManager.  An attached
     * controller should move the state forward until it reports that it is done at which point
     * the loop will exit.
     */
    public static void actionLoop() {
        if (actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS || !actionManager.monsterQueue
                .isEmpty() || shouldStepAiController()) {

            while (shouldWaitOnActions() || shouldStepAiController()) {
                AbstractDungeon.topLevelEffects.clear();
                AbstractDungeon.effectList.clear();
                AbstractDungeon.effectsQueue.clear();

                // TODO this is going to have consequences
                actionManager.cardsPlayedThisCombat.clear();

                if (shouldWaitOnActions()) {
                    while (actionManager.currentAction != null && !AbstractDungeon.isScreenUp) {
                        if (actionManager.currentAction instanceof SetAnimationAction) {
                            actionManager.currentAction = null;
                        } else if (actionManager.currentAction instanceof ShowMoveNameAction) {
                            actionManager.currentAction = null;
                        } else if (actionManager.currentAction instanceof WaitAction) {
                            actionManager.currentAction = null;
                        } else if (actionManager.currentAction instanceof SFXAction) {
                            actionManager.currentAction = null;
                        }

                        if (actionManager.currentAction != null) {
                            if (!actionManager.currentAction.isDone) {
                                actionManager.currentAction.update();
                            }
                        }

                        if (actionManager.currentAction != null &&
                                actionManager.currentAction.isDone && !AbstractDungeon.isScreenUp) {
                            actionManager.currentAction = null;
                        }

                        if (!AbstractDungeon.isScreenUp) {
                            ActionSimulator.ActionManageUpdate();
                        }

                    }
                } else if (shouldStepAiController()) {
                    BattleAiMod.battleAiController.step();
                }

                ActionSimulator.roomUpdate();
            }

            System.err.println("exiting loop ");
            if (actionManager.currentAction == null && !AbstractDungeon.isScreenUp) {
                ActionSimulator.advanceActionQueue();
                AbstractDungeon
                        .getCurrRoom().phase = AbstractRoom.RoomPhase.COMBAT;
            }

        }
    }


    public static void callEndOfTurnActions() {
        AbstractDungeon.getCurrRoom().applyEndOfTurnRelics();
        AbstractDungeon.getCurrRoom().applyEndOfTurnPreCardPowers();

        Iterator var1 = AbstractDungeon.player.hand.group.iterator();

        while (var1.hasNext()) {
            AbstractCard c = (AbstractCard) var1.next();
            c.triggerOnEndOfTurnForPlayingCard();
        }

        AbstractDungeon.player.stance.onEndOfTurn();

        if (!actionManager.actions.isEmpty()) {
            actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;
        }
    }

    /**
     * Based on ActionManager.getNextAction()
     */
    public static void advanceActionQueue() {
        if (!actionManager.actions.isEmpty()) {
            actionManager.currentAction = actionManager.actions.remove(0);
            actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;
            actionManager.hasControl = true;
        } else if (!actionManager.preTurnActions.isEmpty()) {
            actionManager.currentAction = actionManager.preTurnActions.remove(0);
            actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;
            actionManager.hasControl = true;
        } else if (!actionManager.cardQueue.isEmpty()) {
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
        } else if (!actionManager.monsterAttacksQueued) {
            actionManager.monsterAttacksQueued = true;
            if (!AbstractDungeon.getCurrRoom().skipMonsterTurn) {
                AbstractDungeon.getCurrRoom().monsters.queueMonsters();
            }
        } else if (!actionManager.monsterQueue.isEmpty()) {
            AbstractMonster m = actionManager.monsterQueue.get(0).monster;
            if (!m.isDeadOrEscaped() || m.halfDead) {
                m.takeTurn();
                m.applyTurnPowers();
            }
            actionManager.monsterQueue.remove(0);
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
        switch (actionManager.phase) {
            case WAITING_ON_USER:
                ActionSimulator.advanceActionQueue();
                break;
            case EXECUTING_ACTIONS:
                if (actionManager.currentAction != null && !actionManager.currentAction.isDone) {
                    actionManager.currentAction.update();
                } else {
                    actionManager.previousAction = actionManager.currentAction;
                    actionManager.currentAction = null;
                    ActionSimulator.advanceActionQueue();
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
    }

    public static void roomUpdate() {
        updateMonsters();

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
            roomEndTurn();
        }
    }

    public static void roomEndTurn() {
        AbstractDungeon.player.applyEndOfTurnTriggers();

        AbstractDungeon.player.orbs.forEach(AbstractOrb::onEndOfTurn);

        AbstractDungeon.actionManager.addToBottom(new ClearCardQueueAction());
        AbstractDungeon.actionManager.addToBottom(new DiscardAtEndOfTurnAction());

        AbstractDungeon.player.drawPile.group.forEach(AbstractCard::resetAttributes);
        AbstractDungeon.player.discardPile.group.forEach(AbstractCard::resetAttributes);
        AbstractDungeon.player.hand.group.forEach(AbstractCard::resetAttributes);

        if (AbstractDungeon.player.hoveredCard != null) {
            AbstractDungeon.player.hoveredCard.resetAttributes();
        }

        AbstractDungeon.actionManager.addToBottom(new EnqueueEndTurnAction());
        AbstractDungeon.player.isEndingTurn = false;
    }

    public static void updateMonsters() {
        AbstractDungeon.getCurrRoom().monsters.monsters.forEach(monster -> {
            if (monster.isDying) {
                monster.isDead = true;
                monster.dispose();
                monster.powers.clear();
            }

            if (monster.escapeTimer != 0) {
                monster.escaped = true;
            }

            if (AbstractDungeon.getMonsters().areMonstersDead() && !AbstractDungeon
                    .getCurrRoom().isBattleOver && !AbstractDungeon.getCurrRoom().cannotLose) {
                AbstractDungeon.getCurrRoom().endBattle();
            }
        });
    }

    public static boolean shouldStepAiController() {
        if (BattleAiMod.battleAiController == null || BattleAiMod.battleAiController.isDone()) {
            return false;
        }

        if (shouldWaitOnActions()) {
            return false;
        }

        if (AbstractDungeon.isScreenUp) {
            return true;
        }

        return actionManager.phase == GameActionManager.Phase.WAITING_ON_USER &&
                !BattleAiMod.battleAiController.runCommandMode();
    }

    private static boolean shouldWaitOnActions() {
        // Only freeze if the AI is pathing
        if (BattleAiMod.battleAiController == null || BattleAiMod.battleAiController.runCommandMode()) {
            return false;
        }

        // Screens wait for users even though there are actions in the action manager
        if (AbstractDungeon.isScreenUp) {
            return false;
        }

        // Start of Turn
        if (actionManager.turnHasEnded && !AbstractDungeon.getMonsters()
                                                          .areMonstersBasicallyDead()) {
            return true;
        }

        // Middle of Monster turn
        if (!actionManager.monsterQueue.isEmpty()) {
            return true;
        }

        if (actionManager.usingCard) {
            return true;
        }

        return actionManager.currentAction != null || !actionManager.actions
                .isEmpty() || !actionManager.actions
                .isEmpty() || actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS;
    }
}
