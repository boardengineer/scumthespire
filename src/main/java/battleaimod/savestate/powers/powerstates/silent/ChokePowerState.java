package battleaimod.savestate.powers.powerstates.silent;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.ChokePower;

public class ChokePowerState extends PowerState
{
    public ChokePowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new ChokePower(targetAndSource, amount);
    }
}
