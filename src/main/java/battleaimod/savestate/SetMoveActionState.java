package battleaimod.savestate;

import basemod.ReflectionHacks;
import battleaimod.savestate.actions.ActionState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.SetMoveAction;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class SetMoveActionState implements ActionState {
    private final int monsterIndex;
    private final byte theNextMove;
    private final int theNextDamage;
    private final int theMultiplier;
    private final boolean isMultiplier;

    public SetMoveActionState(AbstractGameAction action) {
        this((SetMoveAction) action);
    }

    public SetMoveActionState(SetMoveAction action) {
        monsterIndex = ActionState.indexForCreature(ReflectionHacks
                .getPrivate(action, SetMoveAction.class, "monster"));
        theNextMove = ReflectionHacks.getPrivate(action, SetMoveAction.class, "theNextMove");
        theNextDamage = ReflectionHacks.getPrivate(action, SetMoveAction.class, "theNextDamage");
        theMultiplier = ReflectionHacks.getPrivate(action, SetMoveAction.class, "theMultiplier");
        isMultiplier = ReflectionHacks.getPrivate(action, SetMoveAction.class, "isMultiplier");
    }

    @Override
    public AbstractGameAction loadAction() {
        return new SetMoveAction((AbstractMonster) ActionState
                .creatureForIndex(monsterIndex), theNextMove, AbstractMonster.Intent.UNKNOWN, theNextDamage, theMultiplier, isMultiplier);
    }
}
