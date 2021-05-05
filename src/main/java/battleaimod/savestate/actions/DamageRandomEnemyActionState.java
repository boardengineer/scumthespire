package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageRandomEnemyAction;
import com.megacrit.cardcrawl.cards.DamageInfo;

public class DamageRandomEnemyActionState implements ActionState {
    int amount;
    int ownerIndex;

    DamageInfo.DamageType type;

    public DamageRandomEnemyActionState(AbstractGameAction action) {
        this((DamageRandomEnemyAction) action);
    }

    public DamageRandomEnemyActionState(DamageRandomEnemyAction action) {
        DamageInfo info = ReflectionHacks.getPrivate(action, DamageRandomEnemyAction.class, "info");

        this.ownerIndex = ActionState.indexForCreature(info.owner);
        this.amount = action.amount;
        this.type = info.type;
    }

    @Override
    public AbstractGameAction loadAction() {
        DamageInfo info = new DamageInfo(ActionState.creatureForIndex(ownerIndex), amount, type);

        return new DamageRandomEnemyAction(info, AbstractGameAction.AttackEffect.NONE);
    }
}