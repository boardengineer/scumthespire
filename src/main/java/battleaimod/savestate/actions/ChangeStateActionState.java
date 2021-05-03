package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ChangeStateAction;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class ChangeStateActionState implements ActionState {
    private final int ownerIndex;
    private final String stateName;

    public ChangeStateActionState(AbstractGameAction action) {
        this((ChangeStateAction) action);
    }

    public ChangeStateActionState(ChangeStateAction action) {
        AbstractMonster m = ReflectionHacks.getPrivate(action, ChangeStateAction.class, "m");
        this.ownerIndex = ActionState.indexForCreature(m);
        this.stateName = ReflectionHacks.getPrivate(action, ChangeStateAction.class, "stateName");
    }

    @Override
    public AbstractGameAction loadAction() {
        return new ChangeStateAction((AbstractMonster) ActionState
                .creatureForIndex(ownerIndex), stateName);
    }
}
