package battleaimod.savestate.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.ClearCardQueueAction;

public class ClearCardQueueActionState implements ActionState {
    @Override
    public AbstractGameAction loadAction() {
        return new ClearCardQueueAction();
    }
}
