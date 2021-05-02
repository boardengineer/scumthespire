package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;

public class RemoveSpecificPowerActionState implements ActionState {
    private final String powerId;

    public RemoveSpecificPowerActionState(AbstractGameAction action) {
        this((RemoveSpecificPowerAction) action);
    }

    public RemoveSpecificPowerActionState(RemoveSpecificPowerAction action) {
        AbstractPower power = ReflectionHacks
                .getPrivate(action, RemoveSpecificPowerAction.class, "powerInstance");

        if (power == null) {
            this.powerId = ReflectionHacks
                    .getPrivate(action, RemoveSpecificPowerAction.class, "powerToRemove");
        } else {
            this.powerId = power.ID;
        }
    }

    @Override
    public AbstractGameAction loadAction() {
        RemoveSpecificPowerAction result = new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, powerId);
        return result;
    }
}
