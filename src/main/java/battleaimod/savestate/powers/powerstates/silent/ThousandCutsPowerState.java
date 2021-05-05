package battleaimod.savestate.powers.powerstates.silent;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.ThousandCutsPower;

public class ThousandCutsPowerState extends PowerState
{
    public ThousandCutsPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new ThousandCutsPower(targetAndSource, amount);
    }
}
