package battleaimod.savestate.orbs;

import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Dark;

public class DarkOrbState extends OrbState {
    public DarkOrbState(AbstractOrb orb) {
        super(orb, Orb.DARK.ordinal());
    }

    public DarkOrbState(String jsonString) {
        super(jsonString, Orb.DARK.ordinal());
    }

    @Override
    public AbstractOrb loadOrb() {
        Dark result = new Dark();
        result.evokeAmount = this.evokeAmount;
        result.passiveAmount = this.passiveAmount;
        return result;
    }
}
