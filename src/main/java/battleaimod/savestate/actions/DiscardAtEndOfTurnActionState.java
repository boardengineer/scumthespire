package battleaimod.savestate.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DiscardAtEndOfTurnAction;

public class DiscardAtEndOfTurnActionState implements ActionState {
    @Override
    public AbstractGameAction loadAction() {
        return new DiscardAtEndOfTurnAction();
    }
}
