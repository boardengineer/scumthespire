package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import battleaimod.savestate.CardState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.cards.AbstractCard;

public class MakeTempCardInDrawPileActionState implements ActionState {
    private final CardState cardToMake;
    private final boolean randomSpot;
    private final boolean autoPosition;
    private final boolean toBottom;
    private final int amount;

    public MakeTempCardInDrawPileActionState(AbstractGameAction action) {
        this((MakeTempCardInDrawPileAction) action);
    }

    public MakeTempCardInDrawPileActionState(MakeTempCardInDrawPileAction action) {
        AbstractCard sourceCard = ReflectionHacks
                .getPrivate(action, MakeTempCardInDrawPileAction.class, "cardToMake");
        this.cardToMake = new CardState(sourceCard);
        this.randomSpot = ReflectionHacks
                .getPrivate(action, MakeTempCardInDrawPileAction.class, "randomSpot");
        this.autoPosition = ReflectionHacks
                .getPrivate(action, MakeTempCardInDrawPileAction.class, "autoPosition");
        this.toBottom = ReflectionHacks
                .getPrivate(action, MakeTempCardInDrawPileAction.class, "toBottom");
        this.amount = action.amount;
    }

    @Override
    public AbstractGameAction loadAction() {
        return new MakeTempCardInDrawPileAction(cardToMake
                .loadCard(), amount, randomSpot, autoPosition, toBottom);
    }
}
