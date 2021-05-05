package battleaimod.savestate.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.EscapeAction;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class EscapeActionState implements ActionState {
    private final int sourceIndex;

    public EscapeActionState(AbstractGameAction action) {
        this((EscapeAction) action);
    }

    public EscapeActionState(EscapeAction action) {
        this.sourceIndex = ActionState.indexForCreature(action.source);
    }


    @Override
    public AbstractGameAction loadAction() {
        return new EscapeAction((AbstractMonster) ActionState.creatureForIndex(sourceIndex));
    }
}
