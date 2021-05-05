package battleaimod.savestate.powers.powerstates.watcher;

import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.watcher.StudyPower;

public class StudyPowerState extends PowerState
{
    public StudyPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new StudyPower(targetAndSource, amount);
    }
}
