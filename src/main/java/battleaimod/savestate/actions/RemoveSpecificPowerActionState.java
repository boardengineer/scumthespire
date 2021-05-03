package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;

public class RemoveSpecificPowerActionState implements ActionState {
    private final String powerId;
    private final int ownerIndex;

    public RemoveSpecificPowerActionState(AbstractGameAction action) {
        this((RemoveSpecificPowerAction) action);
    }

    public RemoveSpecificPowerActionState(RemoveSpecificPowerAction action) {
        this.ownerIndex = ActionState.indexForCreature(action.target);

        AbstractPower power = ReflectionHacks
                .getPrivate(action, RemoveSpecificPowerAction.class, "powerInstance");
        if (power == null) {
            this.powerId = ReflectionHacks
                    .getPrivate(action, RemoveSpecificPowerAction.class, "powerToRemove");
        } else {
            this.powerId = power.ID;
        }
    }

    @Override
    public AbstractGameAction loadAction() {
        AbstractCreature target = ActionState.creatureForIndex(ownerIndex);
        RemoveSpecificPowerAction result = new RemoveSpecificPowerAction(target, target, powerId);
        return result;
    }
}
