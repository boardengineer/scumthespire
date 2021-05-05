package battleaimod.savestate.powers.powerstates.defect;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.EquilibriumPower;

public class EquilibriumPowerState extends PowerState
{
    public EquilibriumPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new EquilibriumPower(targetAndSource, amount);
    }
}
