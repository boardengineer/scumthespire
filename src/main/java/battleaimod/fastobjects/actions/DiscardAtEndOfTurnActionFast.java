//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package battleaimod.fastobjects.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.unique.RestoreRetainedCardsAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class DiscardAtEndOfTurnActionFast extends AbstractGameAction {
    public DiscardAtEndOfTurnActionFast() {
        this.duration = 0;
    }

    public void update() {
        Iterator c = AbstractDungeon.player.hand.group.iterator();

        while (true) {
            AbstractCard e;
            do {
                if (!c.hasNext()) {
                    this.addToTop(new RestoreRetainedCardsAction(AbstractDungeon.player.limbo));
                    if (!AbstractDungeon.player.hasRelic("Runic Pyramid") && !AbstractDungeon.player
                            .hasPower("Equilibrium")) {
                        int tempSize = AbstractDungeon.player.hand.size();
                        for (int i = 0; i < tempSize; ++i) {
                            this.addToTop(new DiscardCardActionFast(AbstractDungeon.player, null, AbstractDungeon.player.hand
                                    .size(), true, true));
                        }
                    }

                    ArrayList<AbstractCard> cards = (ArrayList) AbstractDungeon.player.hand.group
                            .clone();
                    Collections.shuffle(cards);
                    Iterator var7 = cards.iterator();

                    while (var7.hasNext()) {
                        AbstractCard card = (AbstractCard) var7.next();
                        card.triggerOnEndOfPlayerTurn();
                    }

                    this.isDone = true;
                    return;
                }

                e = (AbstractCard) c.next();
            } while (!e.retain && !e.selfRetain);

            AbstractDungeon.player.limbo.addToTop(e);
            c.remove();
        }

    }
}
