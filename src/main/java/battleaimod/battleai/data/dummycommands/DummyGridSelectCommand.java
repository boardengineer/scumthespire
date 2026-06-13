package battleaimod.battleai.data.dummycommands;

import battleaimod.utils.FileLogger;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import ludicrousspeed.simulator.commands.Command;
import ludicrousspeed.simulator.commands.GridSelectCommand;

public class DummyGridSelectCommand implements DummyCommand{
    private final AbstractCard card;

    public DummyGridSelectCommand(AbstractCard card) {
        this.card = card;
    }

    @Override
    public Command getRealCommand() {
        int cardIndex = -1;
        int looseCardIndex = -1;
        for(int i = 0; i < AbstractDungeon.gridSelectScreen.targetGroup.size(); ++i) {
            AbstractCard card = AbstractDungeon.gridSelectScreen.targetGroup.group.get(i);
            if (this.card.getMetricID().equals(card.getMetricID())) {
                cardIndex = i;
                break;
            }
            else if (this.card.toString().equals(card.toString()) || this.card.name.equals(card.name)){
                //Fallback: ignore upgrades, handle Armaments or similar situation
                looseCardIndex = i;
            }
        }

        if(cardIndex == -1){
            if(looseCardIndex == -1){
                FileLogger.logError("DummyGridSelectCommand error");
                return null;
            }
            return new GridSelectCommand(looseCardIndex);
        }
        return new GridSelectCommand(cardIndex);

    }
}
