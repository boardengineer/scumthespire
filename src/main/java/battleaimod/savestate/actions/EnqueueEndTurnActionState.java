package battleaimod.savestate.actions;

import battleaimod.fastobjects.ActionSimulator;
import com.megacrit.cardcrawl.actions.AbstractGameAction;

public class EnqueueEndTurnActionState implements ActionState {
    @Override
    public AbstractGameAction loadAction() {
        return new ActionSimulator.EnqueueEndTurnAction();
    }
}
