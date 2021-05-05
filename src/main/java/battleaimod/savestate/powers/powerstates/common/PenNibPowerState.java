package battleaimod.savestate.powers.powerstates.common;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.PenNibPower;

public class PenNibPowerState extends PowerState
{
    public PenNibPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new PenNibPower(targetAndSource, amount);
    }
}
