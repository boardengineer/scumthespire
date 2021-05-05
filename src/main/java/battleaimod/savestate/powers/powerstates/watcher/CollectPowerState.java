package battleaimod.savestate.powers.powerstates.watcher;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.CollectPower;

public class CollectPowerState extends PowerState
{
    public CollectPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new CollectPower(targetAndSource, amount);
    }
}
