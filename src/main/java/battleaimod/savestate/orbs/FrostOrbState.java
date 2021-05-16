package battleaimod.savestate.orbs;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.cards.blue.Blizzard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Frost;

import static battleaimod.savestate.SaveStateMod.shouldGoFast;
import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;

public class FrostOrbState extends OrbState {
    public FrostOrbState(AbstractOrb orb) {
        super(orb, Orb.FROST.ordinal());
    }

    public FrostOrbState(String jsonString) {
        super(jsonString, Orb.FROST.ordinal());
    }

    @Override
    public AbstractOrb loadOrb() {
        Frost result = new Frost();
        result.evokeAmount = this.evokeAmount;
        result.passiveAmount = this.passiveAmount;
        return result;
    }

    @SpirePatch(
            clz = Blizzard.class,
            method = "use",
            paramtypez = {AbstractPlayer.class, AbstractMonster.class}
    )
    public static class NoFXOnUsePatch {
        @SpirePrefixPatch
        public static SpireReturn silentUse(Blizzard blizzard, AbstractPlayer p, AbstractMonster m) {
            if (shouldGoFast) {
                int frostCount = (int) actionManager.orbsChanneledThisCombat
                        .stream()
                        .filter(orb -> orb instanceof Frost)
                        .count();

                blizzard.baseDamage = frostCount + blizzard.magicNumber;
                blizzard.calculateCardDamage(null);

                actionManager
                        .addToBottom(new DamageAllEnemiesAction(p, blizzard.multiDamage, blizzard.damageTypeForTurn, AbstractGameAction.AttackEffect.BLUNT_HEAVY, true));

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
