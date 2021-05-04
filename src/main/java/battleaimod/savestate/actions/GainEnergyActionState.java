package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;

public class GainEnergyActionState implements ActionState {
    private final int energyGain;

    public GainEnergyActionState(AbstractGameAction action) {
        this((GainEnergyAction) action);
    }

    public GainEnergyActionState(GainEnergyAction action) {
        this.energyGain = ReflectionHacks.getPrivate(action, GainEnergyAction.class, "energyGain");
    }

    @Override
    public AbstractGameAction loadAction() {
        return new GainEnergyAction(energyGain);
    }
}
