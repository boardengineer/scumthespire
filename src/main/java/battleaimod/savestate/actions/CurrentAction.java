package battleaimod.savestate.actions;

import battleaimod.fastobjects.actions.DiscardCardActionFast;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.common.ExhaustAction;
import com.megacrit.cardcrawl.actions.unique.ArmamentsAction;
import com.megacrit.cardcrawl.actions.unique.DualWieldAction;

import java.util.function.Function;

public enum CurrentAction {
    ARMAMENTS_ACTION(ArmamentsAction.class, action -> new ArmamentsActionState(action)),
    DISCARD_ACTION(DiscardAction.class, action -> new DiscardActionState(action)),
    DISCARD_ACTION_FAST(DiscardCardActionFast.class, action -> new DiscardActionState(action)),
    DUAL_WIELD_ACTION(DualWieldAction.class, action -> new DualWieldActionState(action)),
    EXHAUST_ACTION(ExhaustAction.class, action -> new ExhaustActionState(action));

    public Function<AbstractGameAction, CurrentActionState> factory;
    public Class<? extends AbstractGameAction> actionClass;

    CurrentAction() {
    }

    CurrentAction(Function<AbstractGameAction, CurrentActionState> factory) {
        this.factory = factory;
    }

    CurrentAction(Class<? extends AbstractGameAction> actionClass, Function<AbstractGameAction, CurrentActionState> factory) {
        this.factory = factory;
        this.actionClass = actionClass;
    }

}
