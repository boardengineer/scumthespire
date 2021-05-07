package battleaimod.savestate.orbs;

import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Frost;

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
}
