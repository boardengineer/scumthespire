package battleaimod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.audio.MusicMaster;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class MusicPatches {
    @SpirePatch(
            clz = MusicMaster.class,
            paramtypez = {String.class, boolean.class},
            method = "playTempBgmInstantly"
    )
    public static class NoPlayMusicPatch3 {
        public static SpireReturn Prefix(MusicMaster _instance, String key, boolean loop) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = MusicMaster.class,
            paramtypez = {String.class},
            method = "playTempBgmInstantly"
    )
    public static class NoPlayMusicPatc2 {
        public static SpireReturn Prefix(MusicMaster _instance, String key) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = MusicMaster.class,
            paramtypez = {String.class},
            method = "playTempBGM"
    )
    public static class NoPlayMusicPatch {
        public static SpireReturn Prefix(MusicMaster _instance, String key) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
