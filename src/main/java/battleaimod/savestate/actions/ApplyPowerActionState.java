package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import battleaimod.savestate.powers.PowerState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;

public class ApplyPowerActionState implements ActionState {
    private final PowerState powerToApply;
    private final int targetIndex;
    private final int amount;

    public ApplyPowerActionState(AbstractGameAction action) {
        this((ApplyPowerAction) action);
    }

    public ApplyPowerActionState(ApplyPowerAction action) {
        this.targetIndex = ActionState.indexForCreature(action.target);
        this.powerToApply = new PowerState((AbstractPower) ReflectionHacks
                .getPrivate(action, ApplyPowerAction.class, "powerToApply"));
        this.amount = action.amount;
    }


    @Override
    public AbstractGameAction loadAction() {
        AbstractCreature target = ActionState.creatureForIndex(targetIndex);
        return new ApplyPowerAction(target, target, powerToApply.loadPower(target), amount);
    }
}
