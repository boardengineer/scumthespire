package communicationmod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.animations.AnimateHopAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.animations.SetAnimationAction;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import communicationmod.CommunicationMod;


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
            clz = GameActionManager.class,
            paramtypez = {},
            method = "update"
    )
    public static class ForceGameActionsPatch {
        private static final AbstractGameAction lastAction = null;

        public static void Postfix(GameActionManager actionManager) {
            if (shouldGoFast()) {
                if (actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS) {
                    while (actionManager.currentAction != null && !actionManager.currentAction.isDone) {
                        actionManager.currentAction.update();
                    }
                    actionManager.update();
                }
            }
        }
    }
}
