package communicationmod.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.metrics.MetricData;
import com.megacrit.cardcrawl.vfx.campfire.CampfireSleepEffect;
import communicationmod.GameStateListener;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class CampfireSleepEffectPatch {

    @SpireInsertPatch(
            locator=LocatorAfter.class
    )
    public static void After(CampfireSleepEffect _instance) {
        GameStateListener.resumeStateUpdate();
    }

    private static class LocatorAfter extends SpireInsertLocator {
        public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
            Matcher matcher = new Matcher.FieldAccessMatcher(CampfireSleepEffect.class, "isDone");
            return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), matcher);
        }
    }

    @SpireInsertPatch(
            locator=LocatorBefore.class
    )
    public static void Before(CampfireSleepEffect _instance) {
        GameStateListener.blockStateUpdate();
    }

    private static class LocatorBefore extends SpireInsertLocator {
        public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
            Matcher matcher = new Matcher.MethodCallMatcher(CampfireSleepEffect.class, "playSleepJingle");
            return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), matcher);
        }
    }
}

