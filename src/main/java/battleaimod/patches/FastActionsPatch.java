package battleaimod.patches;

import basemod.ReflectionHacks;
import battleaimod.BattleAiMod;
import battleaimod.battleai.BattleAiController;
import battleaimod.fastobjects.actions.DrawCardActionFast;
import battleaimod.fastobjects.actions.RollMoveActionFast;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.SetAnimationAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.BottledFlame;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;

import static battleaimod.patches.MonsterPatch.shouldGoFast;
import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;

public class FastActionsPatch {
    @SpirePatch(
            clz = AbstractDungeon.class,
            paramtypez = {},
            method = "update"
    )
    public static class ForceGameActionsPatch {
        public static void Postfix(AbstractDungeon dungeon) {
            GameActionManager actionManager = AbstractDungeon.actionManager;
            if (shouldGoFast()) {
                if (actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS || !actionManager.monsterQueue
                        .isEmpty() || shouldStepAiController()) {
                    while (shouldWaitOnActions(actionManager) || shouldStepAiController()) {

                        // TODO this is going to have consequences
                        actionManager.cardsPlayedThisCombat.clear();

                        long startTime = System.currentTimeMillis();
                        if (shouldWaitOnActions(actionManager)) {
                            if (actionManager.currentAction instanceof RollMoveAction) {
                                AbstractMonster monster = ReflectionHacks
                                        .getPrivate(actionManager.currentAction, RollMoveAction.class, "monster");
                                actionManager.currentAction = new RollMoveActionFast(monster);
                            } else if (actionManager.currentAction instanceof DrawCardAction) {
                                actionManager.currentAction = new DrawCardActionFast(AbstractDungeon.player, actionManager.currentAction.amount);
                            } else if (actionManager.currentAction instanceof SetAnimationAction) {
                                actionManager.currentAction = null;
                            } else if (actionManager.currentAction instanceof ShowMoveNameAction) {
                                actionManager.currentAction = null;
                            }
                            if (actionManager.currentAction != null) {
                                Class actionClass = actionManager.currentAction.getClass();
                                actionManager.currentAction.update();
                                if (BattleAiMod.battleAiController != null) {
                                    long timeThisAction = (System
                                            .currentTimeMillis() - startTime);
                                    BattleAiMod.battleAiController.actionTime += timeThisAction;
                                    HashMap<Class, Long> actionClassTimes = BattleAiMod.battleAiController.actionClassTimes;
                                    if (actionClassTimes != null) {
                                        if (actionClassTimes.containsKey(actionClass)) {
                                            actionClassTimes.put(actionClass, actionClassTimes
                                                    .get(actionClass) + timeThisAction);
                                        } else {
                                            actionClassTimes.put(actionClass, timeThisAction);
                                        }
                                    }
                                }
                            }
                        } else if (shouldStepAiController()) {
                            BattleAiMod.readyForUpdate = false;
                            BattleAiMod.battleAiController.step();
                            BattleAiMod.battleAiController.stepTime += (System
                                    .currentTimeMillis() - startTime);
                        }

                        long preUpdateTime = System.currentTimeMillis();
                        AbstractDungeon.getCurrRoom().update();
                        if (BattleAiMod.battleAiController != null) {
                            BattleAiMod.battleAiController.updateTime += (System
                                    .currentTimeMillis() - preUpdateTime);
                        }
                    }

                    System.err.println("exiting loop");
                }
            }
        }
    }

    @SpirePatch(
            clz = RemoveSpecificPowerAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FastRemovePowerPatch {
        public static void Prefix(RemoveSpecificPowerAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .1F);
            }
        }

        public static void Postfix(RemoveSpecificPowerAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = ApplyPowerAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FastApplyPowerActionPatch {
        public static void Prefix(ApplyPowerAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .1F);
            }
        }

        public static void Postfix(ApplyPowerAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = AbstractRoom.class,
            paramtypez = {},
            method = "endBattle"
    )
    public static class EndBattlePatch {
        public static void Postfix(AbstractRoom _instance) {
            if (shouldGoFast()) {
                BattleAiMod.readyForUpdate = true;
            }
        }
    }

    @SpirePatch(
            clz = MakeTempCardInDiscardAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class MakeTempCardsFastPatch {
        public static void Prefix(MakeTempCardInDiscardAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .001F);

                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "startDuration", .001F);
            }
        }

        public static void Postfix(MakeTempCardInDiscardAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = GainBlockAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class GainBlockActionFastPatch {
        public static void Prefix(GainBlockAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .001F);

                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "startDuration", .001F);
            }
        }

        public static void Postfix(GainBlockAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    private static boolean shouldStepAiController() {
        return BattleAiMod.battleAiController != null && !BattleAiMod.battleAiController.isDone && BattleAiMod.readyForUpdate && actionManager.phase == GameActionManager.Phase.WAITING_ON_USER;
    }

    private static boolean shouldWaitOnActions(GameActionManager actionManager) {
        return actionManager.currentAction != null && !actionManager.currentAction.isDone || !actionManager.monsterQueue
                .isEmpty() || !actionManager.actions.isEmpty();
    }


    @SpirePatch(
            clz = MonsterRoom.class,
            paramtypez = {},
            method = "onPlayerEntry"
    )
    public static class SpyOnMonsterRoomPatch {
        public static void Prefix(MonsterRoom _instance) {
            System.err.println("Starting fight " + AbstractDungeon.monsterList.get(0));
            BattleAiController.currentEncounter = AbstractDungeon.monsterList.get(0);
        }


    }

    @SpirePatch(
            clz = MonsterRoomElite.class,
            paramtypez = {},
            method = "onPlayerEntry"
    )
    public static class SpyOnEliteMonsterRoomPatch {
        public static void Prefix(MonsterRoomElite _instance) {
            System.err.println("Starting fight " + AbstractDungeon.eliteMonsterList.get(0));
            BattleAiController.currentEncounter = AbstractDungeon.eliteMonsterList.get(0);
        }
    }

    @SpirePatch(
            clz = MonsterRoomBoss.class,
            paramtypez = {},
            method = "onPlayerEntry"
    )
    public static class SpyOnBossMonsterRoomPatch {
        public static void Prefix(MonsterRoomBoss _instance) {
            System.err.println("Starting fight " + AbstractDungeon.bossList.get(0));
            BattleAiController.currentEncounter = AbstractDungeon.bossList.get(0);
        }
    }

    // TODO this is a hack to fix an NPE
    @SpirePatch(
            clz = BottledFlame.class,
            paramtypez = {},
            method = "setDescriptionAfterLoading"
    )
    public static class FixDescriptionNPE {
        public static void Replace(BottledFlame _instance) {

        }
    }

    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {},
            method = "draw"
    )
    public static class NoSoundDrawPatch {
        public static void Replace(AbstractPlayer _instance) {
            if (_instance.hand.size() == 10) {
                _instance.createHandIsFullDialog();
            } else {
                _instance.draw(1);
                _instance.onCardDrawOrDiscard();
            }
        }
    }

    // THIS IS VOODOO, DON'T TOUCH IT
    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {int.class},
            method = "draw"
    )
    public static class NoSoundDrawPatch2 {
        private static final Logger logger = LogManager.getLogger(AbstractPlayer.class.getName());

        public static void Replace(AbstractPlayer _instance, int numCards) {
            for (int i = 0; i < numCards; ++i) {
                if (_instance.drawPile.isEmpty()) {
                    logger.info("ERROR: How did this happen? No cards in draw pile?? Player.java");
                } else {
                    AbstractCard c = _instance.drawPile.getTopCard();
                    c.current_x = CardGroup.DRAW_PILE_X;
                    c.current_y = CardGroup.DRAW_PILE_Y;
                    c.setAngle(0.0F, true);
                    c.lighten(false);
                    c.drawScale = 0.12F;
                    c.targetDrawScale = 0.75F;
                    c.triggerWhenDrawn();
                    _instance.hand.addToHand(c);
                    _instance.drawPile.removeTopCard();
                    Iterator var4 = _instance.powers.iterator();

                    while (var4.hasNext()) {
                        AbstractPower p = (AbstractPower) var4.next();
                        p.onCardDraw(c);
                    }

                    var4 = _instance.relics.iterator();

                    while (var4.hasNext()) {
                        AbstractRelic r = (AbstractRelic) var4.next();
                        r.onCardDraw(c);
                    }
                }
            }
        }
    }
}
