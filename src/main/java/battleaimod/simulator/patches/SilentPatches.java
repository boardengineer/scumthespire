package battleaimod.simulator.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.characters.TheSilent;
import savestate.fastobjects.AnimationStateFast;

import static battleaimod.simulator.patches.MonsterPatch.shouldGoFast;

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
}
