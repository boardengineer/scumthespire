package battleaimod.fastobjects;

import battleaimod.BattleAiMod;
import battleaimod.fastobjects.actions.DrawCardActionFast;
import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.IntentFlashAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.EnableEndTurnButtonAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAndEnableControlsAction;
import com.megacrit.cardcrawl.actions.common.ShowMoveNameAction;
import com.megacrit.cardcrawl.actions.defect.TriggerEndOfTurnOrbsAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.blights.AbstractBlight;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.daily.mods.Careless;
import com.megacrit.cardcrawl.daily.mods.ControlledChaos;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.TipTracker;
import com.megacrit.cardcrawl.helpers.input.DevInputActionSet;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.UnceasingTop;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.vfx.ThoughtBubble;
import com.megacrit.cardcrawl.vfx.cardManip.ExhaustCardEffect;
import com.megacrit.cardcrawl.vfx.combat.BattleStartEffect;

import java.util.Iterator;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;

/**
 *  This contains static methods that are optimized versions of methods from GameActionAManager,
 *  actions can use this variant to update faster
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
        long localManagerNextAction = System.currentTimeMillis();
        if (!AbstractDungeon.actionManager.actions.isEmpty()) {
            AbstractDungeon.actionManager.currentAction = AbstractDungeon.actionManager.actions
                    .remove(0);
            AbstractDungeon.actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;
            AbstractDungeon.actionManager.hasControl = true;
        } else if (!AbstractDungeon.actionManager.preTurnActions.isEmpty()) {
            AbstractDungeon.actionManager.currentAction = AbstractDungeon.actionManager.preTurnActions
                    .remove(0);
            AbstractDungeon.actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;
            AbstractDungeon.actionManager.hasControl = true;
        } else if (!AbstractDungeon.actionManager.cardQueue.isEmpty()) {
            long startCardStuff = System.currentTimeMillis();

            AbstractDungeon.actionManager.usingCard = true;
            AbstractCard c = AbstractDungeon.actionManager.cardQueue.get(0).card;
            if (c == null) {
                ActionSimulator.callEndOfTurnActions();
            } else if (c.equals(AbstractDungeon.actionManager.lastCard)) {
                AbstractDungeon.actionManager.lastCard = null;
            }

            if (AbstractDungeon.actionManager.cardQueue
                    .size() == 1 && AbstractDungeon.actionManager.cardQueue
                    .get(0).isEndTurnAutoPlay) {
                AbstractRelic top = AbstractDungeon.player.getRelic("Unceasing Top");
                if (top != null) {
                    ((UnceasingTop) top).disableUntilTurnEnds();
                }
            }

            boolean canPlayCard = false;
            if (c != null) {
                c.isInAutoplay = AbstractDungeon.actionManager.cardQueue
                        .get(0).autoplayCard;
            }

            if (c != null && AbstractDungeon.actionManager.cardQueue
                    .get(0).randomTarget) {
                AbstractDungeon.actionManager.cardQueue
                        .get(0).monster = AbstractDungeon.getMonsters()
                                                         .getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
            }

            Iterator i;
            AbstractCard card;
            if (AbstractDungeon.actionManager.cardQueue.get(0).card == null || !c
                    .canUse(AbstractDungeon.player, actionManager.cardQueue
                            .get(0).monster) && !actionManager.cardQueue
                    .get(0).card.dontTriggerOnUseCard) {
                i = AbstractDungeon.player.limbo.group.iterator();

                while (i.hasNext()) {
                    card = (AbstractCard) i.next();
                    if (card == c) {
                        c.fadingOut = true;
                        AbstractDungeon.effectList.add(new ExhaustCardEffect(c));
                        i.remove();
                    }
                }

                if (c != null) {
                    AbstractDungeon.effectList
                            .add(new ThoughtBubble(AbstractDungeon.player.dialogX, AbstractDungeon.player.dialogY, 3.0F, c.cantUseMessage, true));
                }
            } else {
                canPlayCard = true;
                if (c.freeToPlay()) {
                    c.freeToPlayOnce = true;
                }

                AbstractDungeon.actionManager.cardQueue
                        .get(0).card.energyOnUse = AbstractDungeon.actionManager.cardQueue
                        .get(0).energyOnUse;
                if (c.isInAutoplay) {
                    AbstractDungeon.actionManager.cardQueue
                            .get(0).card.ignoreEnergyOnUse = true;
                } else {
                    AbstractDungeon.actionManager.cardQueue
                            .get(0).card.ignoreEnergyOnUse = AbstractDungeon.actionManager.cardQueue
                            .get(0).ignoreEnergyTotal;
                }

                if (!AbstractDungeon.actionManager.cardQueue
                        .get(0).card.dontTriggerOnUseCard) {
                    i = AbstractDungeon.player.powers.iterator();

                    while (i.hasNext()) {
                        AbstractPower p = (AbstractPower) i.next();
                        p.onPlayCard(AbstractDungeon.actionManager.cardQueue
                                .get(0).card, AbstractDungeon.actionManager.cardQueue
                                .get(0).monster);
                    }

                    i = AbstractDungeon.getMonsters().monsters.iterator();

                    while (i.hasNext()) {
                        AbstractMonster m = (AbstractMonster) i.next();
                        Iterator var5 = m.powers.iterator();

                        while (var5.hasNext()) {
                            AbstractPower p = (AbstractPower) var5.next();
                            p.onPlayCard(AbstractDungeon.actionManager.cardQueue
                                    .get(0).card, AbstractDungeon.actionManager.cardQueue
                                    .get(0).monster);
                        }
                    }

                    i = AbstractDungeon.player.relics.iterator();

                    while (i.hasNext()) {
                        AbstractRelic r = (AbstractRelic) i.next();
                        r.onPlayCard(AbstractDungeon.actionManager.cardQueue
                                .get(0).card, AbstractDungeon.actionManager.cardQueue
                                .get(0).monster);
                    }

                    AbstractDungeon.player.stance
                            .onPlayCard(AbstractDungeon.actionManager.cardQueue
                                    .get(0).card);
                    i = AbstractDungeon.player.blights.iterator();

                    while (i.hasNext()) {
                        AbstractBlight b = (AbstractBlight) i.next();
                        b.onPlayCard(AbstractDungeon.actionManager.cardQueue
                                .get(0).card, AbstractDungeon.actionManager.cardQueue
                                .get(0).monster);
                    }

                    i = AbstractDungeon.player.hand.group.iterator();

                    while (i.hasNext()) {
                        card = (AbstractCard) i.next();
                        card.onPlayCard(AbstractDungeon.actionManager.cardQueue
                                .get(0).card, AbstractDungeon.actionManager.cardQueue
                                .get(0).monster);
                    }

                    i = AbstractDungeon.player.discardPile.group.iterator();

                    while (i.hasNext()) {
                        card = (AbstractCard) i.next();
                        card.onPlayCard(AbstractDungeon.actionManager.cardQueue
                                .get(0).card, AbstractDungeon.actionManager.cardQueue
                                .get(0).monster);
                    }

                    i = AbstractDungeon.player.drawPile.group.iterator();

                    while (i.hasNext()) {
                        card = (AbstractCard) i.next();
                        card.onPlayCard(AbstractDungeon.actionManager.cardQueue
                                .get(0).card, AbstractDungeon.actionManager.cardQueue
                                .get(0).monster);
                    }

                    ++AbstractDungeon.player.cardsPlayedThisTurn;
                    AbstractDungeon.actionManager.cardsPlayedThisTurn
                            .add(AbstractDungeon.actionManager.cardQueue
                                    .get(0).card);
                    AbstractDungeon.actionManager.cardsPlayedThisCombat
                            .add(AbstractDungeon.actionManager.cardQueue
                                    .get(0).card);
                }

                if (AbstractDungeon.actionManager.cardsPlayedThisTurn.size() == 25) {
                    UnlockTracker.unlockAchievement("INFINITY");
                }

                if (AbstractDungeon.actionManager.cardsPlayedThisTurn
                        .size() >= 20 && !CardCrawlGame.combo) {
                    CardCrawlGame.combo = true;
                }

                if (AbstractDungeon.actionManager.cardQueue
                        .get(0).card instanceof Shiv) {
                    int shivCount = 0;
                    Iterator var15 = AbstractDungeon.actionManager.cardsPlayedThisTurn.iterator();

                    while (var15.hasNext()) {
                        AbstractCard card2 = (AbstractCard) var15.next();
                        if (card2 instanceof Shiv) {
                            ++shivCount;
                            if (shivCount == 10) {
                                UnlockTracker.unlockAchievement("NINJA");
                                break;
                            }
                        }
                    }
                }

                if (AbstractDungeon.actionManager.cardQueue.get(0).card != null) {
                    if (AbstractDungeon.actionManager.cardQueue
                            .get(0).card.target != AbstractCard.CardTarget.ENEMY || AbstractDungeon.actionManager.cardQueue
                            .get(0).monster != null && !AbstractDungeon.actionManager.cardQueue
                            .get(0).monster.isDeadOrEscaped()) {
                        AbstractDungeon.player
                                .useCard(AbstractDungeon.actionManager.cardQueue
                                        .get(0).card, AbstractDungeon.actionManager.cardQueue
                                        .get(0).monster, AbstractDungeon.actionManager.cardQueue
                                        .get(0).energyOnUse);
                    } else {
                        i = AbstractDungeon.player.limbo.group.iterator();

                        while (i.hasNext()) {
                            card = (AbstractCard) i.next();
                            if (card == AbstractDungeon.actionManager.cardQueue
                                    .get(0).card) {
                                AbstractDungeon.actionManager.cardQueue
                                        .get(0).card.fadingOut = true;
                                AbstractDungeon.effectList
                                        .add(new ExhaustCardEffect(AbstractDungeon.actionManager.cardQueue
                                                .get(0).card));
                                i.remove();
                            }
                        }

                        if (AbstractDungeon.actionManager.cardQueue
                                .get(0).monster == null) {
                            AbstractDungeon.actionManager.cardQueue
                                    .get(0).card.drawScale = AbstractDungeon.actionManager.cardQueue
                                    .get(0).card.targetDrawScale;
                            AbstractDungeon.actionManager.cardQueue
                                    .get(0).card.angle = AbstractDungeon.actionManager.cardQueue
                                    .get(0).card.targetAngle;
                            AbstractDungeon.actionManager.cardQueue
                                    .get(0).card.current_x = AbstractDungeon.actionManager.cardQueue
                                    .get(0).card.target_x;
                            AbstractDungeon.actionManager.cardQueue
                                    .get(0).card.current_y = AbstractDungeon.actionManager.cardQueue
                                    .get(0).card.target_y;
                            AbstractDungeon.effectList
                                    .add(new ExhaustCardEffect(AbstractDungeon.actionManager.cardQueue
                                            .get(0).card));
                        }
                    }
                }
            }

            AbstractDungeon.actionManager.cardQueue.remove(0);
            if (!canPlayCard && c != null && c.isInAutoplay) {
                c.dontTriggerOnUseCard = true;
                AbstractDungeon.actionManager.addToBottom(new UseCardAction(c));
            }

            if (BattleAiMod.battleAiController != null) {
                BattleAiMod.battleAiController.addRuntime("Local Action Manager Card Stuff", System
                        .currentTimeMillis() - startCardStuff);
            }

        } else if (!AbstractDungeon.actionManager.monsterAttacksQueued) {
            long startMonsterQueuedStuff = System.currentTimeMillis();

            AbstractDungeon.actionManager.monsterAttacksQueued = true;
            if (!AbstractDungeon.getCurrRoom().skipMonsterTurn) {
                AbstractDungeon.getCurrRoom().monsters.queueMonsters();
            }

            if (BattleAiMod.battleAiController != null) {
                BattleAiMod.battleAiController
                        .addRuntime("Local Action Manager Monster Queued Stuff", System
                                .currentTimeMillis() - startMonsterQueuedStuff);
            }
        } else if (!AbstractDungeon.actionManager.monsterQueue.isEmpty()) {
            long startOtherMonsterQueuedStuff = System.currentTimeMillis();

            AbstractMonster m = AbstractDungeon.actionManager.monsterQueue
                    .get(0).monster;
            if (!m.isDeadOrEscaped() || m.halfDead) {
                if (m.intent != AbstractMonster.Intent.NONE) {
                    AbstractDungeon.actionManager.addToBottom(new ShowMoveNameAction(m));
                    AbstractDungeon.actionManager.addToBottom(new IntentFlashAction(m));
                }

                if (!(Boolean) TipTracker.tips
                        .get("INTENT_TIP") && AbstractDungeon.player.currentBlock == 0 && (m.intent == AbstractMonster.Intent.ATTACK || m.intent == AbstractMonster.Intent.ATTACK_DEBUFF || m.intent == AbstractMonster.Intent.ATTACK_BUFF || m.intent == AbstractMonster.Intent.ATTACK_DEFEND)) {
                    if (AbstractDungeon.floorNum <= 5) {
                        ++TipTracker.blockCounter;
                    } else {
                        TipTracker.neverShowAgain("INTENT_TIP");
                    }
                }

                long monsterTurnStart = System.currentTimeMillis();

                m.takeTurn();
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController.addRuntime("Monster Turn", System
                            .currentTimeMillis() - monsterTurnStart);
                }

                m.applyTurnPowers();
            }

            AbstractDungeon.actionManager.monsterQueue.remove(0);
            if (AbstractDungeon.actionManager.monsterQueue.isEmpty()) {
                AbstractDungeon.actionManager.addToBottom(new WaitAction(1.5F));
            }

            if (BattleAiMod.battleAiController != null) {
                BattleAiMod.battleAiController
                        .addRuntime("Local Action Manager Other Monster Queued Stuff", System
                                .currentTimeMillis() - startOtherMonsterQueuedStuff);
            }
        } else if (AbstractDungeon.actionManager.turnHasEnded && !AbstractDungeon.getMonsters()
                                                                                 .areMonstersBasicallyDead()) {
            long startEOTStuff = System.currentTimeMillis();

            if (!AbstractDungeon.getCurrRoom().skipMonsterTurn) {
                AbstractDungeon.getCurrRoom().monsters.applyEndOfTurnPowers();
            }

            AbstractDungeon.player.cardsPlayedThisTurn = 0;
            AbstractDungeon.actionManager.orbsChanneledThisTurn.clear();
            if (ModHelper.isModEnabled("Careless")) {
                Careless.modAction();
            }

            if (ModHelper.isModEnabled("ControlledChaos")) {
                ControlledChaos.modAction();
                AbstractDungeon.player.hand.applyPowers();
            }

            long startStartOfTurn = System.currentTimeMillis();

            AbstractDungeon.player.applyStartOfTurnRelics();
            AbstractDungeon.player.applyStartOfTurnPreDrawCards();
            AbstractDungeon.player.applyStartOfTurnCards();
            AbstractDungeon.player.applyStartOfTurnPowers();
            AbstractDungeon.player.applyStartOfTurnOrbs();

            if (BattleAiMod.battleAiController != null) {
                BattleAiMod.battleAiController.addRuntime("EOT Stuff SOT", System
                        .currentTimeMillis() - startStartOfTurn);
            }

            ++GameActionManager.turn;
            AbstractDungeon.getCurrRoom().skipMonsterTurn = false;
            AbstractDungeon.actionManager.turnHasEnded = false;
            GameActionManager.totalDiscardedThisTurn = 0;
            AbstractDungeon.actionManager.cardsPlayedThisTurn.clear();
            GameActionManager.damageReceivedThisTurn = 0;
            if (!AbstractDungeon.player.hasPower("Barricade") && !AbstractDungeon.player
                    .hasPower("Blur")) {
                if (!AbstractDungeon.player.hasRelic("Calipers")) {
                    AbstractDungeon.player.loseBlock();
                } else {
                    AbstractDungeon.player.loseBlock(15);
                }
            }

            long startLastPart = System.currentTimeMillis();

            if (!AbstractDungeon.getCurrRoom().isBattleOver) {
                long checkpoint1 = System.currentTimeMillis();
                AbstractDungeon.actionManager
                        .addToBottom(new DrawCardActionFast(null, AbstractDungeon.player.gameHandSize, true));

                long checkpoint2 = System.currentTimeMillis();
                AbstractDungeon.player.applyStartOfTurnPostDrawRelics();
                long checkpoint3 = System.currentTimeMillis();

                AbstractDungeon.player.applyStartOfTurnPostDrawPowers();
                long checkpoint4 = System.currentTimeMillis();

                AbstractDungeon.actionManager.addToBottom(new EnableEndTurnButtonAction());
                long checkpoint5 = System.currentTimeMillis();

                if (BattleAiMod.battleAiController != null) {
//                    BattleAiMod.battleAiController.addRuntime("EOT part1", checkpoint2 - checkpoint1);
//                    BattleAiMod.battleAiController.addRuntime("EOT part2", checkpoint3 - checkpoint2);
//                    BattleAiMod.battleAiController.addRuntime("EOT part3", checkpoint4 - checkpoint3);
//                    BattleAiMod.battleAiController.addRuntime("EOT part4", checkpoint5 - checkpoint4);
                }
            }

            if (BattleAiMod.battleAiController != null) {
                BattleAiMod.battleAiController.addRuntime("Local Action Manager EOT Stuff", System
                        .currentTimeMillis() - startEOTStuff);
                BattleAiMod.battleAiController.addRuntime("EOT Stuff end part", System
                        .currentTimeMillis() - startLastPart);
            }
        }

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController.addRuntime("Local Manager Next Action", System
                    .currentTimeMillis() - localManagerNextAction);
        }
    }

    public static void ActionManageUpdate() {
        long localManagerUpdateStart = System.currentTimeMillis();
        switch (AbstractDungeon.actionManager.phase) {
            case WAITING_ON_USER:
                ActionSimulator.ActionManagerNextAction();
                break;
            case EXECUTING_ACTIONS:
                if (AbstractDungeon.actionManager.currentAction != null && !AbstractDungeon.actionManager.currentAction.isDone) {
                    if (actionManager.currentAction instanceof DrawCardAction) {
                        actionManager.currentAction = new DrawCardActionFast(AbstractDungeon.player, actionManager.currentAction.amount);
                    }
                    AbstractDungeon.actionManager.currentAction.update();
                } else {
                    AbstractDungeon.actionManager.previousAction = AbstractDungeon.actionManager.currentAction;
                    AbstractDungeon.actionManager.currentAction = null;
                    ActionSimulator.ActionManagerNextAction();
                    if (AbstractDungeon.actionManager.currentAction == null && AbstractDungeon
                            .getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && !actionManager.usingCard) {
                        AbstractDungeon.actionManager.phase = GameActionManager.Phase.WAITING_ON_USER;
                        AbstractDungeon.player.hand.refreshHandLayout();
                        AbstractDungeon.actionManager.hasControl = false;
                    }

                    AbstractDungeon.actionManager.usingCard = false;
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
            long startTopCondition = System.currentTimeMillis();

            if (Settings.isDebug && DevInputActionSet.drawCard.isJustPressed()) {
                AbstractDungeon.actionManager
                        .addToTop(new DrawCardAction(AbstractDungeon.player, 1));
            }

            long midTopCondition = System.currentTimeMillis();

            if (!AbstractDungeon.isScreenUp) {
//                System.err.println("updating room " + actionManager.cardsPlayedThisCombat
//                        .size() + " " + actionManager.cardsPlayedThisTurn.size());

                ActionSimulator.ActionManageUpdate();
                if (!AbstractDungeon.getCurrRoom().monsters
                        .areMonstersBasicallyDead() && AbstractDungeon.player.currentHealth > 0) {
                    AbstractDungeon.player.updateInput();
                }
            }


            if (!AbstractDungeon.screen.equals(AbstractDungeon.CurrentScreen.HAND_SELECT)) {
                AbstractDungeon.player.combatUpdate();
            }

            if (AbstractDungeon.player.isEndingTurn) {
                AbstractDungeon.getCurrRoom().endTurn();
            }


            if (BattleAiMod.battleAiController != null) {
//                BattleAiMod.battleAiController.addRuntime("Top Condition", System
//                        .currentTimeMillis() - startTopCondition);
//                BattleAiMod.battleAiController.addRuntime("Mid Top Condition", System
//                        .currentTimeMillis() - midTopCondition);
            }

        } else {
            if (AbstractDungeon.actionManager.currentAction == null && AbstractDungeon.actionManager
                    .isEmpty()) {
                AbstractDungeon.getCurrRoom().waitTimer -= Gdx.graphics.getDeltaTime();
            } else {
                AbstractDungeon.actionManager.update();
            }

            if (AbstractDungeon.getCurrRoom().waitTimer <= 0.0F) {
                AbstractDungeon.actionManager.turnHasEnded = true;
                if (!AbstractDungeon.isScreenUp) {
                    AbstractDungeon.topLevelEffects.add(new BattleStartEffect(false));
                }

                AbstractDungeon.actionManager
                        .addToBottom(new GainEnergyAndEnableControlsAction(AbstractDungeon.player.energy.energyMaster));
                AbstractDungeon.player.applyStartOfCombatPreDrawLogic();
                AbstractDungeon.actionManager
                        .addToBottom(new DrawCardActionFast(AbstractDungeon.player, AbstractDungeon.player.gameHandSize));
                AbstractDungeon.actionManager.addToBottom(new EnableEndTurnButtonAction());
                AbstractDungeon.overlayMenu.showCombatPanels();
                AbstractDungeon.player.applyStartOfCombatLogic();
                if (ModHelper.isModEnabled("Careless")) {
                    Careless.modAction();
                }

                if (ModHelper.isModEnabled("ControlledChaos")) {
                    ControlledChaos.modAction();
                }

                AbstractDungeon.getCurrRoom().skipMonsterTurn = false;
                AbstractDungeon.player.applyStartOfTurnRelics();
                AbstractDungeon.player.applyStartOfTurnPostDrawRelics();
                AbstractDungeon.player.applyStartOfTurnCards();
                AbstractDungeon.player.applyStartOfTurnPowers();
                AbstractDungeon.player.applyStartOfTurnOrbs();
                AbstractDungeon.actionManager.useNextCombatActions();
            }
        }

        if (AbstractDungeon.getCurrRoom().isBattleOver && AbstractDungeon.actionManager.actions
                .isEmpty()) {

            // There was stuff here, hopefully we don't need it
        }

        AbstractDungeon.getCurrRoom().monsters.updateAnimations();
    }
}
