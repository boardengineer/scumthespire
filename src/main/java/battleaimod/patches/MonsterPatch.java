package battleaimod.patches;

import battleaimod.BattleAiMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.animations.AnimateHopAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.animations.SetAnimationAction;
import com.megacrit.cardcrawl.actions.common.SpawnMonsterAction;
import com.megacrit.cardcrawl.monsters.AbstractMonster;


public class MonsterPatch {
    public static boolean shouldGoFast() {
        return BattleAiMod.goFast;
//        return true;
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
            clz = AbstractMonster.class,
            paramtypez = {boolean.class},
            method = "die"
    )
    public static class MonsterDeathWithRelicsPatch {
        public static void Postfix(AbstractMonster _instance, boolean triggerRelics) {
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
        public static void Prefix(SetAnimationAction _instance) {
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
        public static void Prefix(AnimateHopAction _instance) {
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
        public static void Prefix(AnimateSlowAttackAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = SpawnMonsterAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class SpawnMonsterAnimationPatch {
        public static void Prefix(SpawnMonsterAction _instance) {
            if (shouldGoFast()) {
                System.err.println("spawning");
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
        public static void Prefix(AnimateFastAttackAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }
}
