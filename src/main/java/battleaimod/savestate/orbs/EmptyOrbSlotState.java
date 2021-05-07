package battleaimod.savestate.orbs;

import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.EmptyOrbSlot;

public class EmptyOrbSlotState extends OrbState {
    public EmptyOrbSlotState(AbstractOrb orb) {
        super(orb, Orb.EMPTY.ordinal());
    }

    public EmptyOrbSlotState(String jsonString) {
        super(jsonString, Orb.EMPTY.ordinal());
    }

    @Override
    public AbstractOrb loadOrb() {
        EmptyOrbSlot result = new EmptyOrbSlot();
        result.evokeAmount = this.evokeAmount;
        result.passiveAmount = this.passiveAmount;
        return result;
    }
}
