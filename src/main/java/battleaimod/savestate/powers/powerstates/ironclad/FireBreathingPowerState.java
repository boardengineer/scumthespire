package battleaimod.savestate.powers.powerstates.ironclad;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.FireBreathingPower;

public class FireBreathingPowerState extends PowerState
{
    public FireBreathingPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new FireBreathingPower(targetAndSource, amount);
    }
}
