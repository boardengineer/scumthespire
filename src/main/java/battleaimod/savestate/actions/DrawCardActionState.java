package battleaimod.savestate.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;

public class DrawCardActionState implements ActionState {
    private final int amount;

    public DrawCardActionState(AbstractGameAction action) {
        this.amount = action.amount;
    }

    @Override
    public AbstractGameAction loadAction() {
        DrawCardAction result = new DrawCardAction(amount);
        return result;
    }
}
