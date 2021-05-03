package battleaimod.savestate.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;

public class GainBlockActionState implements ActionState {
    private final int ownerIndex;
    private final int amount;

    public GainBlockActionState(AbstractGameAction action) {
        this((GainBlockAction) action);
    }

    public GainBlockActionState(GainBlockAction action) {
        this.ownerIndex = ActionState.indexForCreature(action.target);
        this.amount = action.amount;
    }

    @Override
    public AbstractGameAction loadAction() {
        return new GainBlockAction(ActionState.creatureForIndex(ownerIndex), amount);
    }
}
