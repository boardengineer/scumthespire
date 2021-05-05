package battleaimod.savestate.powers.powerstates.monsters;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.ExplosivePower;

public class ExplosivePowerState extends PowerState
{
    public ExplosivePowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new ExplosivePower(targetAndSource, amount);
    }
}
