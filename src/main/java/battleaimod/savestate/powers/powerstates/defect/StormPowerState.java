package battleaimod.savestate.powers.powerstates.defect;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StormPower;

public class StormPowerState extends PowerState
{
    public StormPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new StormPower(targetAndSource, amount);
    }
}
