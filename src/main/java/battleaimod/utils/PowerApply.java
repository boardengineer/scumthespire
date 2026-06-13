package battleaimod.utils;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.powers.AbstractPower;

@SpirePatch(
        clz = ApplyPowerAction.class,
        method = "update"
)

public class PowerApply {

    @SpirePostfixPatch
    public static void afterApplyPower(ApplyPowerAction __instance) {

        //powerToApply is private. Can't access it without Reflection.
        AbstractPower power = ReflectionHacks.getPrivate(__instance, ApplyPowerAction.class, "powerToApply");

        if (power != null) {
            FitnessTracker.onPowerApply(power);
        }
    }
}