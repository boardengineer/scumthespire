package battleaimod.savestate.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.EnvenomPower;

public class EnvenomPowerState extends PowerState {
    public EnvenomPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new EnvenomPower(targetAndSource, amount);
    }
}