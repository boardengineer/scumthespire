package battleaimod.savestate.powers.powerstates.monsters;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.TimeWarpPower;

public class TimeWarpPowerState extends PowerState
{
    public TimeWarpPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        TimeWarpPower timeWarpPower = new TimeWarpPower(targetAndSource);
        timeWarpPower.amount = this.amount;
        return timeWarpPower;
    }
}
