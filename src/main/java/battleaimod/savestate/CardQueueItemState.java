package battleaimod.savestate;

import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class CardQueueItemState {
    private final CardState card;
    private final int monsterIndex;
    private final int energyOnUse;
    private final boolean ignoreEnergyTotal;
    private final boolean autoplayCard;
    private final boolean randomTarget;
    private final boolean isEndTurnAutoPlay;

    public CardQueueItemState(CardQueueItem queueItem) {
        this.card = new CardState(queueItem.card);

        int indexCandidate = -1;
        for (int i = 0; i < AbstractDungeon.getCurrRoom().monsters.monsters.size(); i++) {
            if (queueItem.monster == AbstractDungeon.getCurrRoom().monsters.monsters.get(i)) {
                indexCandidate = i;
            }
        }

        monsterIndex = indexCandidate;
        energyOnUse = queueItem.energyOnUse;
        ignoreEnergyTotal = queueItem.ignoreEnergyTotal;
        autoplayCard = queueItem.autoplayCard;
        randomTarget = queueItem.randomTarget;
        isEndTurnAutoPlay = queueItem.isEndTurnAutoPlay;
    }

    public CardQueueItem loadQueueItem() {
        CardQueueItem result = new CardQueueItem(card.loadCard(), isEndTurnAutoPlay);

        if (monsterIndex != -1) {
            result.monster = AbstractDungeon.getMonsters().monsters.get(monsterIndex);
        }
        AbstractDungeon.actionManager
                .addToBottom(new UseCardAction(card.loadCard(), result.monster));

        result.energyOnUse = energyOnUse;
        result.ignoreEnergyTotal = ignoreEnergyTotal;
        result.autoplayCard = autoplayCard;
        result.randomTarget = randomTarget;
        result.isEndTurnAutoPlay = isEndTurnAutoPlay;

        return result;
    }
}
