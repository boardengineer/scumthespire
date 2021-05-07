package battleaimod.savestate.orbs;

import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Lightning;

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
}
