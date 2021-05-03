package battleaimod.savestate.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.FeelNoPainPower;

public class FeelNoPainPowerState extends PowerState {
    public FeelNoPainPowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new FeelNoPainPower(targetAndSource, amount);
    }
}
