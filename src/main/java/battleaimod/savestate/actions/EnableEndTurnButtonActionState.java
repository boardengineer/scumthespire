package battleaimod.savestate.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.EnableEndTurnButtonAction;

public class EnableEndTurnButtonActionState implements ActionState {
    public EnableEndTurnButtonActionState(AbstractGameAction action) {}

    public EnableEndTurnButtonActionState(EnableEndTurnButtonAction action) {}

    @Override
    public AbstractGameAction loadAction() {
        return new EnableEndTurnButtonAction();
    }
}
