package battleaimod.savestate.powers.powerstates;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.watcher.BlockReturnPower;

public class BlockReturnPowerState extends PowerState
{
    public BlockReturnPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new BlockReturnPower(targetAndSource, amount);
    }
}
