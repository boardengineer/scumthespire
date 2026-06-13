package battleaimod.battleai.data.dummycommands;

import battleaimod.utils.FileLogger;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import ludicrousspeed.simulator.commands.Command;
import ludicrousspeed.simulator.commands.HandSelectCommand;

public class DummyHandSelectCommand implements DummyCommand{
    private final AbstractCard card;

    public DummyHandSelectCommand(AbstractCard card) {
        this.card = card;
    }

    @Override
    public Command getRealCommand() {
        int cardIndex = DummyCommand.getCardIndexFromHand(card);

        if(cardIndex == -1){
            FileLogger.logError("DummyHandSelectCommand error");
            FileLogger.logError("card not found: " + card + " id: " + card.getMetricID());
            FileLogger.logError("hand: " + AbstractDungeon.player.hand.group);
            for (AbstractCard c : AbstractDungeon.player.hand.group){
                FileLogger.logError("   card id: " + c.getMetricID());
            }
            return null;
        }

        return new HandSelectCommand(cardIndex);
    }
}

