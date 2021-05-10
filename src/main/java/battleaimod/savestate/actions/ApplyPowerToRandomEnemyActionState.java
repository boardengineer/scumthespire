package battleaimod.savestate.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerToRandomEnemyAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.PoisonPower;

public class ApplyPowerToRandomEnemyActionState implements ActionState {
    private final int amount;
    private final int sourceIndex;

    public ApplyPowerToRandomEnemyActionState(AbstractGameAction action) {
        this.amount = action.amount;
        this.sourceIndex = ActionState.indexForCreature(action.source);
    }

    @Override
    public AbstractGameAction loadAction() {
        ApplyPowerToRandomEnemyAction result = new ApplyPowerToRandomEnemyAction(ActionState
                .creatureForIndex(sourceIndex), new PoisonPower(null, AbstractDungeon.player, amount));
        return result;
    }
}
