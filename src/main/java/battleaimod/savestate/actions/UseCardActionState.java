package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import battleaimod.fastobjects.actions.UpdateOnlyUseCardAction;
import battleaimod.savestate.CardState;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;

public class UseCardActionState implements ActionState {
    private final CardState card;

    public UseCardActionState(UseCardAction action) {
        AbstractCard card = ReflectionHacks.getPrivate(action, UseCardAction.class, "targetCard");
        this.card = new CardState(card);
    }

    public UseCardActionState(UpdateOnlyUseCardAction action) {
        AbstractCard card = ReflectionHacks
                .getPrivate(action, UpdateOnlyUseCardAction.class, "targetCard");
        this.card = new CardState(card);
    }

    @Override
    public UpdateOnlyUseCardAction loadAction() {
        AbstractCard resultCard = card.loadCard();

        // TODO: at some point the target here will matter
        return new UpdateOnlyUseCardAction(resultCard, null);
    }
}
