package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class DamageActionState implements ActionState {
    private final DamageInfo info;
    private final int targetIndex;
    private final int ownerIndex;

    public DamageActionState(AbstractGameAction action) {
        this((DamageAction) action);
    }

    public DamageActionState(DamageAction action) {
        info = ReflectionHacks.getPrivate(action, DamageAction.class, "info");

        ownerIndex = ActionState.indexForCreature(info.owner);
        targetIndex = ActionState.indexForCreature(action.target);
    }

    @Override
    public AbstractGameAction loadAction() {
        info.owner = ActionState.creatureForIndex(ownerIndex);

        AbstractCreature target = ActionState.creatureForIndex(targetIndex);

        return new DamageAction(ActionState
                .creatureForIndex(targetIndex), info, AbstractGameAction.AttackEffect.BLUNT_LIGHT);
    }
}
