package battleaimod.simulator.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.helpers.File;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;

import static battleaimod.simulator.patches.MonsterPatch.shouldGoFast;

public class SavePatches {
    @SpirePatch(
            clz = SaveFile.class,
            paramtypez = {SaveFile.SaveType.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoMakeSavePatch {
        public static SpireReturn Prefix(SaveFile _instance, SaveFile.SaveType type) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = SaveAndContinue.class,
            paramtypez = {SaveFile.class},
            method = "save"
    )
    public static class NoSavingPatch {
        public static SpireReturn Prefix(SaveFile save) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = File.class,
            paramtypez = {},
            method = "save"
    )
    public static class NoSavingOnOtherThreadPatch {
        public static SpireReturn Prefix(File _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
