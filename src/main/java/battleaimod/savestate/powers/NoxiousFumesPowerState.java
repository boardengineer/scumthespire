package battleaimod.savestate.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.NoxiousFumesPower;

public class NoxiousFumesPowerState extends PowerState {
    public NoxiousFumesPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new NoxiousFumesPower(targetAndSource, amount);
    }
}
