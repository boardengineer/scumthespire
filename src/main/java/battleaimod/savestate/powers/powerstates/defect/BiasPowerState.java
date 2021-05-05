package battleaimod.savestate.powers.powerstates.defect;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.BiasPower;

public class BiasPowerState extends PowerState
{
    public BiasPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new BiasPower(targetAndSource, amount);
    }
}
