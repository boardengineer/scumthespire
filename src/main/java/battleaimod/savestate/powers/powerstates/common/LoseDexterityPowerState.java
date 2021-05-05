package battleaimod.savestate.powers.powerstates.common;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.LoseDexterityPower;

public class LoseDexterityPowerState extends PowerState
{
    public LoseDexterityPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new LoseDexterityPower(targetAndSource, amount);
    }
}
