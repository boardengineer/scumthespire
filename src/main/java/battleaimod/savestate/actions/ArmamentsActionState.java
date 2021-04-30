package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import battleaimod.savestate.CardState;
import battleaimod.savestate.PlayerState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.unique.ArmamentsAction;
import com.megacrit.cardcrawl.cards.AbstractCard;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ArmamentsActionState {
    private final ArrayList<CardState> cannotUpgrade;
    private final boolean upgraded;

    public ArmamentsActionState(AbstractGameAction action) {
        this((ArmamentsAction) action);
    }

    public ArmamentsActionState(ArmamentsAction action) {
        ArrayList<AbstractCard> cannotUpgradeSource = ReflectionHacks
                .getPrivate(action, ArmamentsAction.class, "cannotUpgrade");
        cannotUpgrade = PlayerState.toCardStateArray(cannotUpgradeSource);

        upgraded = ReflectionHacks
                .getPrivate(action, ArmamentsAction.class, "upgraded");
    }

    public ArmamentsAction loadAction() {
        ArmamentsAction result = new ArmamentsAction(upgraded);

        ReflectionHacks
                .setPrivate(result, ArmamentsAction.class, "cannotUpgrade", cannotUpgrade.stream()
                                                                                         .map(CardState::loadCard)
                                                                                         .collect(Collectors
                                                                                                 .toCollection(ArrayList::new)));
        ReflectionHacks
                .setPrivate(result, AbstractGameAction.class, "duration", 0);

        return result;
    }
}
