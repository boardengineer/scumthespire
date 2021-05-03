package battleaimod.savestate.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class LoseHPActionState implements ActionState {
    private final int amount;
    private final AbstractCreature target;

    public LoseHPActionState(AbstractGameAction action) {
        this((LoseHPAction) action);
    }

    public LoseHPActionState(LoseHPAction action) {
        this.amount = action.amount;
        this.target = action.target;
    }

    @Override
    public AbstractGameAction loadAction() {
        return new LoseHPAction(target, AbstractDungeon.player, amount);
    }
}
