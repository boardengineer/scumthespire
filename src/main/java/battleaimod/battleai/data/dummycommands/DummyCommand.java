package battleaimod.battleai.data.dummycommands;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import ludicrousspeed.simulator.commands.Command;

public interface DummyCommand {
    public Command getRealCommand();

    public static int getCardIndexFromHand(AbstractCard card){
        int cardIndex = -1;
        int looseCardIndex = -1;

        for (int i = 0; i < AbstractDungeon.player.hand.group.size(); i++) {
            AbstractCard handCard = AbstractDungeon.player.hand.group.get(i);

            if (handCard.getMetricID().equals(card.getMetricID())) {
                cardIndex = i;
                break;
            }
            else if(handCard.toString().equals(card.toString()) || handCard.name.equals(card.name)){
                //Fallback: ignore upgrades, handle Armaments or similar situation
                looseCardIndex = i;
            }
        }

        if(cardIndex == -1){
            return looseCardIndex; //Returns -1 if not found
        }
        return cardIndex;
    }
}
