package battleaimod.patches;

import battleaimod.BattleAiMod;
import battleaimod.fastobjects.AnimationStateFast;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.Ironclad;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class IroncladPatches {
    @SpirePatch(
            clz = Ironclad.class,
            paramtypez = {DamageInfo.class},
            method = "damage"
    )
    public static class SpyOnPlayerDamageffectPatch {
        static long startEffect = 0;

        public static SpireReturn Prefix(Ironclad _instance, DamageInfo info) {
            if (shouldGoFast()) {
                startEffect = System.currentTimeMillis();
//                System.err.println(info.base + " " + info.type);
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }

        public static SpireReturn Postfix(Ironclad _instance, DamageInfo info) {
            if (shouldGoFast()) {
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController
                            .addRuntime("Ironclad damage", System
                                    .currentTimeMillis() - startEffect);
                }
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = Ironclad.class,
            paramtypez = {String.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 79)
        public static SpireReturn Sentry(Ironclad _instance, String playerName) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {SpriteBatch.class},
            method = "render"
    )
    public static class DisableRenderIroncladPatch {
        public static SpireReturn Prefix(AbstractPlayer _instance, SpriteBatch sprites) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
