package battleaimod.savestate.powers.powerstates.ironclad;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.RupturePower;

public class RupturePowerState extends PowerState
{
    public RupturePowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new RupturePower(targetAndSource, amount);
    }
}
