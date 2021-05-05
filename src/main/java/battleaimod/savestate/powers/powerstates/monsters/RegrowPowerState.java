package battleaimod.savestate.powers.powerstates.monsters;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.RegrowPower;

public class RegrowPowerState extends PowerState
{
    public RegrowPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new RegrowPower(targetAndSource);
    }
}
