package battleaimod.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class DamageActionPatches {
    @SpirePatch(
            clz = DamageAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class SpyOnDamageUpdatePatch {
        static long startUpdate = 0;

        public static void Prefix(DamageAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks.setPrivate(_instance, AbstractGameAction.class, "duration", .1F);
                startUpdate = System.currentTimeMillis();
            }
        }

        public static void Postfix(DamageAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }
}
