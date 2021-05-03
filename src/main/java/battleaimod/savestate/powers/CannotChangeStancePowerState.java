package battleaimod.savestate.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.watcher.CannotChangeStancePower;

public class CannotChangeStancePowerState extends PowerState {
    public CannotChangeStancePowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new CannotChangeStancePower(targetAndSource);
    }
}
