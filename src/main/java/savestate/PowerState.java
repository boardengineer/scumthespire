package savestate;

import com.megacrit.cardcrawl.powers.AbstractPower;

public class PowerState {
    private final AbstractPower power;
    private final int amount;

    public PowerState(AbstractPower power) {
        this.power = power;
        this.amount = power.amount;
    }

    public AbstractPower loadPower() {
        power.amount = amount;
        return power;
    }

}
