package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class RollMoveActionState implements ActionState {
    private final int monsterIndex;

    public RollMoveActionState(AbstractGameAction action) {
        this((RollMoveAction) action);
    }

    public RollMoveActionState(RollMoveAction action) {
        this.monsterIndex = ActionState.indexForCreature(ReflectionHacks
                .getPrivate(action, RollMoveAction.class, "monster"));
    }

    @Override
    public AbstractGameAction loadAction() {
        return new RollMoveAction((AbstractMonster) ActionState.creatureForIndex(monsterIndex));
    }
}
