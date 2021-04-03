package communicationmod.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.animations.AnimateHopAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.animations.SetAnimationAction;
import com.megacrit.cardcrawl.actions.common.EnableEndTurnButtonAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.SpikeSlime_M;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToDiscardEffect;
import communicationmod.CommunicationMod;
import fastobjects.actions.RollMoveActionFast;


public class MonsterPatch {
    public static boolean shouldGoFast() {
        return true || CommunicationMod.battleAiController != null && CommunicationMod.battleAiController.runCommandMode;
    }

    @SpirePatch(
            clz = AbstractMonster.class,
            paramtypez = {},
            method = "die"
    )
    public static class MonsterDeathPatch {
        public static void Postfix(AbstractMonster _instance) {
            if (shouldGoFast()) {
                _instance.deathTimer = .0000001F;
            }
        }
    }

    @SpirePatch(
            clz = SetAnimationAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class SetAnimationPatch {
        public static void Replace(SetAnimationAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = AnimateHopAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class HopAnimationPatch {
        public static void Replace(AnimateHopAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = AnimateSlowAttackAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class SlowAttackAnimationPatch {
        public static void Replace(AnimateSlowAttackAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = AnimateFastAttackAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FastAttackAnimationPatch {
        public static void Replace(AnimateFastAttackAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }


    @SpirePatch(
            clz = ShowCardAndAddToDiscardEffect.class,
            paramtypez = {AbstractCard.class},
            method = "<ctor>"
    )
    public static class ShowCardAndAddToDiscardEffectPatch {
        Patcher pp;
        public static void Prefix(ShowCardAndAddToDiscardEffect _instance, AbstractCard card) {
            System.err.println("starting constructor " + card);
        }

        public static void Postfix(ShowCardAndAddToDiscardEffect _instance, AbstractCard card) {
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
            clz = EnableEndTurnButtonAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class InformEndTurnEnabledActionPatch {
        public static void Postfix(EnableEndTurnButtonAction _instance) {
            if (shouldGoFast()) {
                CommunicationMod.readyForUpdate = true;
            }
        }
    }

    @SpirePatch(
            clz = SpikeSlime_M.class,
            paramtypez = {},
            method = "takeTurn"
    )
    public static class SpikeSlime_MPatch {
        public static void Postfix(SpikeSlime_M _instance) {
            System.err.println("slime took a turn");
        }
    }

    @SpirePatch(
            clz = GameActionManager.class,
            paramtypez = {},
            method = "update"
    )
    public static class ForceGameActionsPatch {
        private static final AbstractGameAction lastAction = null;

        public static void Postfix(GameActionManager actionManager) {
            if (shouldGoFast()) {
                if (actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS || !actionManager.monsterQueue
                        .isEmpty()) {
                    while (actionManager.currentAction != null && !actionManager.currentAction.isDone) {
                        if(actionManager.currentAction instanceof RollMoveAction) {
                            AbstractMonster monster = ReflectionHacks
                                    .getPrivate(actionManager.currentAction, RollMoveAction.class, "monster");

                            actionManager.currentAction = new RollMoveActionFast(monster);
                        }
                        actionManager.currentAction.update();
                    }
                    actionManager.update();
                }
            }
        }
    }
}
