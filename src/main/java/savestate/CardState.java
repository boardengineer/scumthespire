package savestate;

import com.megacrit.cardcrawl.cards.AbstractCard;

public class CardState {
    private final AbstractCard card;
    private final boolean upgraded;

    public CardState(AbstractCard card) {
        this.card = card;
        upgraded = card.upgraded;
    }

    public AbstractCard loadCard() {
        card.upgraded = upgraded;
        return card;
    }
}
