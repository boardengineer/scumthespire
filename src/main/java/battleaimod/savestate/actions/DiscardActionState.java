package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class DiscardActionState implements CurrentActionState {
    private final int amount;

    public DiscardActionState(DiscardAction action) { amount = action.amount; }

    @Override
    public AbstractGameAction loadCurrentAction() {
        DiscardAction result = new DiscardAction(AbstractDungeon.player, AbstractDungeon.player, amount, false);

        ReflectionHacks.setPrivate(result, AbstractGameAction.class, "duration", 0);

        return result;
    }
}
