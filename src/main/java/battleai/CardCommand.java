package battleai;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import communicationmod.CommunicationMod;

public class CardCommand implements Command {
    private final AbstractCard card;
    private final AbstractMonster monster;

    public CardCommand(AbstractCard card, AbstractMonster monster) {
        this.card = card;
        this.monster = monster;
    }

    @Override
    public void execute() {
        AbstractDungeon.actionManager.cardQueue.add(new CardQueueItem(card, monster));
        CommunicationMod.readyForUpdate = true;
        System.out.println("really setting to true");
    }

    @Override
    public String toString() {
        return String.format("Use %s on %s", card.name, monster == null ? "self" : monster.name);
    }
}
