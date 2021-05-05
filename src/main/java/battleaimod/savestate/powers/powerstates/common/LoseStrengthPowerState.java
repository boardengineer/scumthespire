package battleaimod.savestate.powers.powerstates.common;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.LoseStrengthPower;

public class LoseStrengthPowerState extends PowerState
{
    public LoseStrengthPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new LoseStrengthPower(targetAndSource, amount);
    }
}
