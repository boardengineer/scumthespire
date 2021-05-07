package battleaimod.savestate.orbs;

import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Plasma;

public class PlasmaOrbState extends OrbState {
    public PlasmaOrbState(AbstractOrb orb) {
        super(orb, Orb.PLASMA.ordinal());
    }

    public PlasmaOrbState(String jsonString) {
        super(jsonString, Orb.PLASMA.ordinal());
    }

    @Override
    public AbstractOrb loadOrb() {
        Plasma result = new Plasma();
        result.evokeAmount = this.evokeAmount;
        result.passiveAmount = this.passiveAmount;
        return result;
    }
}
