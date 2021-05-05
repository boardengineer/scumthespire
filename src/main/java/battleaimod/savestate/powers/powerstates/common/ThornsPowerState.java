package battleaimod.savestate.powers.powerstates.common;

import battleaimod.savestate.powers.PowerState;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.ThornsPower;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class ThornsPowerState extends PowerState {
    public ThornsPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new ThornsPower(targetAndSource, amount);
    }

    @SpirePatch(clz = ThornsPower.class, method = "onAttacked")
    public static class NoFlashPatch {
        public static SpireReturn Prefix(ThornsPower power, DamageInfo info, int damageAmount) {
            if (shouldGoFast()) {
                if (info.type != DamageInfo.DamageType.THORNS && info.type != DamageInfo.DamageType.HP_LOSS && info.owner != null && info.owner != power.owner) {
                    AbstractDungeon.actionManager
                            .addToTop(new DamageAction(info.owner, new DamageInfo(power.owner, power.amount, DamageInfo.DamageType.THORNS), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL, true));
                }

                return SpireReturn.Return(damageAmount);
            }

            return SpireReturn.Continue();
        }
    }
}
