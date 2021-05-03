package battleaimod.savestate.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.DarkEmbracePower;

public class DarkEmbracePowerState extends PowerState {
    public DarkEmbracePowerState(AbstractPower power) {
        super(power);
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new DarkEmbracePower(targetAndSource, amount);
    }
}
