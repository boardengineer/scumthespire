package battleaimod.savestate.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.watcher.WaveOfTheHandPower;

public class WaveOfTheHandPowerState extends PowerState {
    public WaveOfTheHandPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new WaveOfTheHandPower(targetAndSource, amount);
    }
}
