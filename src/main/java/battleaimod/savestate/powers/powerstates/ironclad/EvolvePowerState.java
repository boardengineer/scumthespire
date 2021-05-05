package battleaimod.savestate.powers.powerstates.ironclad;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.EvolvePower;

public class EvolvePowerState extends PowerState
{
    public EvolvePowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new EvolvePower(targetAndSource, amount);
    }
}
