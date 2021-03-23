package communicationmod;

import com.megacrit.cardcrawl.cards.AbstractCard;

public class CardLoader {
    private final AbstractCard card;
    private final boolean upgraded;

    public CardLoader(AbstractCard card) {
        this.card = card;
        upgraded = card.upgraded;
    }

    public AbstractCard loadCard() {
        card.upgraded = upgraded;
        return card;
    }
}
