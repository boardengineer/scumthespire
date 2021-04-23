package battleaimod.fastobjects;

import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.AbstractCard.CardType;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;
import java.util.Iterator;

public class NoLoggerMummifiedHand extends AbstractRelic {
    public NoLoggerMummifiedHand() {
        super("Mummified Hand", "mummifiedHand.png", RelicTier.UNCOMMON, LandingSound.FLAT);
    }

    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0];
    }

    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.type == CardType.POWER) {
            this.flash();
            this.addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
            ArrayList<AbstractCard> groupCopy = new ArrayList();
            Iterator var4 = AbstractDungeon.player.hand.group.iterator();

            while (true) {
                while (var4.hasNext()) {
                    AbstractCard c = (AbstractCard) var4.next();
                    if (c.cost > 0 && c.costForTurn > 0 && !c.freeToPlayOnce) {
                        groupCopy.add(c);
                    }
                }

                var4 = AbstractDungeon.actionManager.cardQueue.iterator();

                while (var4.hasNext()) {
                    CardQueueItem i = (CardQueueItem) var4.next();
                    if (i.card != null) {
                        groupCopy.remove(i.card);
                    }
                }

                AbstractCard c = null;
                if (groupCopy.isEmpty()) {
                } else {
                    Iterator var9 = groupCopy.iterator();

                    while (var9.hasNext()) {
                        AbstractCard cc = (AbstractCard) var9.next();
                    }

                    c = groupCopy
                            .get(AbstractDungeon.cardRandomRng.random(0, groupCopy.size() - 1));
                }

                if (c != null) {
                    c.setCostForTurn(0);
                } else {
                }
                break;
            }
        }

    }

    public AbstractRelic makeCopy() {
        return new NoLoggerMummifiedHand();
    }
}
