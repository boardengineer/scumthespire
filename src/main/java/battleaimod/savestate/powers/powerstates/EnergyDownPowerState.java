package battleaimod.savestate.powers.powerstates;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.watcher.EnergyDownPower;

public class EnergyDownPowerState extends PowerState
{
    public EnergyDownPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new EnergyDownPower(targetAndSource, amount);
    }
}
