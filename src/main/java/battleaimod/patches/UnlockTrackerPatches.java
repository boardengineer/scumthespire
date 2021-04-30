package battleaimod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.unlock.UnlockTracker;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class UnlockTrackerPatches {
    @SpirePatch(
            clz = UnlockTracker.class,
            paramtypez = {String.class},
            method = "hardUnlockOverride"
    )
    public static class NoHardOverrideUnlockTrackerPatch {
        public static SpireReturn Prefix(String cardName) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = UnlockTracker.class,
            paramtypez = {String.class},
            method = "hardUnlock"
    )
    public static class NoHardUnlockTrackerPatch {
        public static SpireReturn Prefix(String cardName) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = UnlockTracker.class,
            paramtypez = {String.class},
            method = "markCardAsSeen"
    )
    public static class NoUnlockTrackerPatch {
        public static SpireReturn Prefix(String cardName) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
