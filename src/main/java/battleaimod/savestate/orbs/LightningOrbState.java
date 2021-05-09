package battleaimod.savestate.orbs;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Lightning;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class LightningOrbState extends OrbState {
    public LightningOrbState(AbstractOrb orb) {
        super(orb, Orb.LIGHTNING.ordinal());
    }

    public LightningOrbState(String jsonString) {
        super(jsonString, Orb.LIGHTNING.ordinal());
    }

    @Override
    public AbstractOrb loadOrb() {
        Lightning result = new Lightning();
        result.evokeAmount = this.evokeAmount;
        result.passiveAmount = this.passiveAmount;
        return result;
    }

    @SpirePatch(clz = Lightning.class, method = "onEndOfTurn")
    public static class LightningEOTPatch {
        @SpirePrefixPatch
        public static SpireReturn doEOT(Lightning lightning) {
            if (shouldGoFast()) {
                // TODO electro power
                AbstractCreature m = AbstractDungeon.getRandomMonster();
                DamageInfo info = new DamageInfo(AbstractDungeon.player, lightning.passiveAmount, DamageInfo.DamageType.THORNS);
                AbstractDungeon.actionManager
                        .addToTop(new DamageAction(m, info, AbstractGameAction.AttackEffect.NONE, true));
                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }
}
