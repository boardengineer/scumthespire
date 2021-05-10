package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.unique.DiscardPileToTopOfDeckAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

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

    @SpirePatch(
            clz = DiscardPileToTopOfDeckAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoDoubleDualWieldPatch {
        public static void Postfix(DiscardPileToTopOfDeckAction _instance) {
            // Force the action to stay in the the manager until cards are selected
            if (AbstractDungeon.isScreenUp) {
                _instance.isDone = false;
            }
        }
    }
}
