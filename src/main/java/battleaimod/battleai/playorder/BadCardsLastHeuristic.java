package battleaimod.battleai.playorder;

import com.megacrit.cardcrawl.cards.AbstractCard;

import java.util.Comparator;

public class BadCardsLastHeuristic implements Comparator<AbstractCard> {
    @Override
    public int compare(AbstractCard card1, AbstractCard card2) {
        if (card1.type == AbstractCard.CardType.CURSE) {
            if (card2.type != AbstractCard.CardType.CURSE) {
                return 1;
            }
        } else if (card2.type == AbstractCard.CardType.CURSE) {
            return -1;
        }

        if (card1.type == AbstractCard.CardType.STATUS) {
            if (card2.type != AbstractCard.CardType.STATUS) {
                return 1;
            }
        } else if (card2.type == AbstractCard.CardType.STATUS) {
            return -1;
        }

        if (isBasic(card1)) {
            if (!isBasic(card2)) {
                return 1;
            }
        } else if (isBasic(card2)) {
            return -1;
        }

        return 0;
    }

    private static boolean isBasic(AbstractCard card) {
        return card.hasTag(AbstractCard.CardTags.STARTER_DEFEND) || card
                .hasTag(AbstractCard.CardTags.STARTER_STRIKE);
    }
}