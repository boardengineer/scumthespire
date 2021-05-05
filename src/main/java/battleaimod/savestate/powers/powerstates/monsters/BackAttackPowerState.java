package battleaimod.savestate.powers.powerstates.monsters;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.BackAttackPower;

public class BackAttackPowerState extends PowerState
{
    public BackAttackPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new BackAttackPower(targetAndSource);
    }
}
