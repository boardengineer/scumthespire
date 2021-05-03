package battleaimod.savestate.actions;

import battleaimod.fastobjects.actions.DiscardCardActionFast;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class DiscardCardActionState implements ActionState {
    private final int amount;

    public DiscardCardActionState(AbstractGameAction action) {
        this((DiscardCardActionFast) action);
    }

    public DiscardCardActionState(DiscardCardActionFast action) {
        amount = action.amount;
    }

    @Override
    public AbstractGameAction loadAction() {
        DiscardCardActionFast result = new DiscardCardActionFast(AbstractDungeon.player, AbstractDungeon.player, amount, false);
        result.secondHalfOnly = true;
        return result;
    }
}
