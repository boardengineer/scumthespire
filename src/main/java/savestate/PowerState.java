package savestate;

import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;

public class PowerState {
    private final AbstractPower power;
    private final int amount;

    public PowerState(AbstractPower power) {
        this.power = power;
        this.amount = power.amount;
//        System.err.printf("Saving power of type %s and amount %s\n", power.getClass(), amount);

    }

    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        power.owner = targetAndSource;
        power.amount = amount;
        return power;
    }

}
