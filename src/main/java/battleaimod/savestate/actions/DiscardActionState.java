package battleaimod.savestate.actions;

import battleaimod.fastobjects.actions.DiscardCardActionFast;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class DiscardActionState implements ActionState
{
    public final int amount;
    
    public DiscardActionState(DiscardAction action) {
        amount = action.amount;
    }
    
    public DiscardActionState(DiscardCardActionFast action) {
        amount = action.amount;
    }
    
    @Override
    public AbstractGameAction loadAction() {
        DiscardCardActionFast result = new DiscardCardActionFast(AbstractDungeon.player, AbstractDungeon.player, amount, false);
        result.secondHalfOnly = true;
        return result;
    }
}
