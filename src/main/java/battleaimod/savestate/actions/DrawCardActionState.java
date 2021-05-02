package battleaimod.savestate.actions;

import battleaimod.fastobjects.actions.DrawCardActionFast;
import com.megacrit.cardcrawl.actions.AbstractGameAction;

public class DrawCardActionState implements ActionState {
    private final int amount;

    public DrawCardActionState(AbstractGameAction action) {
        this.amount = action.amount;
    }

    @Override
    public AbstractGameAction loadAction() {
        DrawCardActionFast result = new DrawCardActionFast(amount);
        return result;
    }
}
