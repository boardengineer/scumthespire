package battleaimod.savestate.powers.powerstates.ironclad;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.BrutalityPower;

public class BrutalityPowerState extends PowerState
{
    public BrutalityPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new BrutalityPower(targetAndSource, amount);
    }
}
