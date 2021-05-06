package battleaimod.savestate.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;

public interface CurrentActionState {
    AbstractGameAction loadCurrentAction();
}
