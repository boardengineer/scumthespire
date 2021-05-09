package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.unique.RetainCardsAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class RetainCardsActionState implements CurrentActionState {
    private final int amount;

    public RetainCardsActionState(AbstractGameAction action) {
        this.amount = action.amount;
    }

    @Override
    public AbstractGameAction loadCurrentAction() {
        RetainCardsAction result = new RetainCardsAction(AbstractDungeon.player, amount);

        ReflectionHacks
                .setPrivate(result, AbstractGameAction.class, "duration", 0);

        return result;
    }
}
