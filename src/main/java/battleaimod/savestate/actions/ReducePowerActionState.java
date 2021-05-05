package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;

public class ReducePowerActionState implements ActionState {
    private final String powerID;
    int amount;
    int targetIndex;

    public ReducePowerActionState(AbstractGameAction action) {
        this((ReducePowerAction) action);
    }

    public ReducePowerActionState(ReducePowerAction action) {
        AbstractPower sourcePower = ReflectionHacks
                .getPrivate(action, ReducePowerAction.class, "powerInstance");
        if (sourcePower != null) {
            powerID = sourcePower.ID;
        } else {
            powerID = ReflectionHacks
                    .getPrivate(action, ReducePowerAction.class, "powerID");
        }

        amount = action.amount;
        targetIndex = ActionState.indexForCreature(action.target);
    }

    @Override
    public AbstractGameAction loadAction() {
        AbstractCreature targetAndSource = ActionState.creatureForIndex(targetIndex);
        return new ReducePowerAction(targetAndSource, targetAndSource, powerID, amount);
    }
}
