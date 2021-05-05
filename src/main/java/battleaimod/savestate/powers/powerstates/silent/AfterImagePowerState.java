package battleaimod.savestate.powers.powerstates.silent;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.AfterImagePower;

public class AfterImagePowerState extends PowerState
{
    public AfterImagePowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new AfterImagePower(targetAndSource, amount);
    }
}
