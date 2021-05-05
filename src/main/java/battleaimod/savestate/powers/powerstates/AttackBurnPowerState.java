package battleaimod.savestate.powers.powerstates;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.AttackBurnPower;

public class AttackBurnPowerState extends PowerState
{
    public AttackBurnPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new AttackBurnPower(targetAndSource, amount);
    }
}
