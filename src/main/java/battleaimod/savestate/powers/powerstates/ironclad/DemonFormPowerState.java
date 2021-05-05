package battleaimod.savestate.powers.powerstates.ironclad;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.DemonFormPower;

public class DemonFormPowerState extends PowerState
{
    public DemonFormPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new DemonFormPower(targetAndSource, amount);
    }
}
