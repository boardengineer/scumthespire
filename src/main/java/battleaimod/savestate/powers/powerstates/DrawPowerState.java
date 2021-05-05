package battleaimod.savestate.powers.powerstates;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.DrawPower;

public class DrawPowerState extends PowerState
{
    public DrawPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new DrawPower(targetAndSource, amount);
    }
}
