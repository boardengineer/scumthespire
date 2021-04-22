package battleaimod.patches;

import basemod.ReflectionHacks;
import battleaimod.BattleAiMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class DamageActionPatches {
    @SpirePatch(
            clz = DamageAction.class,
            paramtypez = {AbstractCreature.class, DamageInfo.class, AbstractGameAction.AttackEffect.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class SpyOnDmgFxConstructorPatch {
        static long startConstructor = 0;

        public static SpireReturn Prefix(DamageAction _instance, AbstractCreature source, DamageInfo info, AbstractGameAction.AttackEffect effect) {
            if (shouldGoFast()) {
                startConstructor = System.currentTimeMillis();
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }

        public static SpireReturn Postfix(DamageAction _instance, AbstractCreature source, DamageInfo info, AbstractGameAction.AttackEffect effect) {
            if (shouldGoFast()) {
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController.addRuntime("Damage Action Constructor ", System
                            .currentTimeMillis() - startConstructor);
                }
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = DamageAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class SpyOnDamageUpdatePatch {
        static long startUpdate = 0;

        public static SpireReturn Prefix(DamageAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks.setPrivate(_instance, AbstractGameAction.class, "duration", .1F);
                startUpdate = System.currentTimeMillis();
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }

        public static SpireReturn Postfix(DamageAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController.addRuntime("Damage Action Update ", System
                            .currentTimeMillis() - startUpdate);
                }
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = FlashAtkImgEffect.class,
            paramtypez = {float.class, float.class, AbstractGameAction.AttackEffect.class, boolean.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class SpyOnFlashFxConstructorPatch {
        static long startConstructor = 0;

        public static SpireReturn Prefix(FlashAtkImgEffect _instance, float x, float y, AbstractGameAction.AttackEffect effect, boolean mute) {
            if (shouldGoFast()) {
                startConstructor = System.currentTimeMillis();
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }

        public static SpireReturn Postfix(FlashAtkImgEffect _instance, float x, float y, AbstractGameAction.AttackEffect effect, boolean mute) {
            if (shouldGoFast()) {
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController.addRuntime("FlashAtkImgEffect Constructor ", System
                            .currentTimeMillis() - startConstructor);
                }
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }
    }
}
