package battleaimod.savestate.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.NoBlockPower;

public class NoBlockPowerState extends PowerState {
    public NoBlockPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        // TODO
        return new NoBlockPower(targetAndSource, amount, false);
    }
}
