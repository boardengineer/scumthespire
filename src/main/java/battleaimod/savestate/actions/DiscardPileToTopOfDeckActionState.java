package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.unique.DiscardPileToTopOfDeckAction;

public class DiscardPileToTopOfDeckActionState implements CurrentActionState {
    int amount;
    int sourceIndex;

    DiscardPileToTopOfDeckActionState(AbstractGameAction action) {
        this.amount = action.amount;
        this.sourceIndex = ActionState.indexForCreature(action.source);
    }

    @Override
    public AbstractGameAction loadCurrentAction() {
        DiscardPileToTopOfDeckAction result = new DiscardPileToTopOfDeckAction(ActionState
                .creatureForIndex(sourceIndex));
        result.amount = amount;

        // This should make the action only trigger the second half of the update
        ReflectionHacks
                .setPrivate(result, AbstractGameAction.class, "duration", 0);

        return result;
    }
}
