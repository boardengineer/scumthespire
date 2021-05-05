package battleaimod.savestate.powers.powerstates.monsters;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.GenericStrengthUpPower;

public class GenericStrengthUpPowerState extends PowerState
{
    public GenericStrengthUpPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        // TODO
        return new GenericStrengthUpPower(targetAndSource,"", amount);
    }
}
