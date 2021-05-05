package battleaimod.patches;

import battleaimod.BattleAiMod;
import battleaimod.fastobjects.AnimationStateFast;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.TheSilent;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SilentPatches {
    @SpirePatch(
            clz = TheSilent.class,
            paramtypez = {String.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoSilentAnimationsPatch {
        @SpireInsertPatch(loc = 70)
        public static SpireReturn Insert(TheSilent _instance, String playerName) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = TheSilent.class,
            paramtypez = {DamageInfo.class},
            method = "damage"
    )
    public static class SpyOnTheSilentDamagePatch {
        static long startEffect = 0;

        public static SpireReturn Prefix(TheSilent _instance, DamageInfo info) {
            if (shouldGoFast()) {
                startEffect = System.currentTimeMillis();
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }

        public static SpireReturn Postfix(TheSilent _instance, DamageInfo info) {
            if (shouldGoFast()) {
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController
                            .addRuntime("Silent damage", System
                                    .currentTimeMillis() - startEffect);
                }
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }
    }
}
