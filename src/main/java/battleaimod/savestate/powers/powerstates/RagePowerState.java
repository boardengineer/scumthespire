package battleaimod.savestate.powers.powerstates;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.RagePower;

public class RagePowerState extends PowerState
{
    public RagePowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new RagePower(targetAndSource, amount);
    }
}
