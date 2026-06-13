package battleaimod.battleai.data.dummycommands;

import battleaimod.battleai.data.CardAction;
import battleaimod.utils.FileLogger;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import ludicrousspeed.simulator.commands.CardCommand;
import ludicrousspeed.simulator.commands.Command;

import java.util.List;
import java.util.Objects;

public class DummyCardCommand implements DummyCommand {
    private final AbstractCard card;
    private final int enemyIndex;
    private boolean valid = true;

    public DummyCardCommand(AbstractCard card, int enemyIndex) {
        this.card = card;
        this.enemyIndex = enemyIndex;
    }

    public DummyCardCommand(AbstractCard card) {
        this.card = card;
        this.enemyIndex = -1;
    }

    @Override
    public Command getRealCommand() {
        int cardIndex = DummyCommand.getCardIndexFromHand(card);

        //TODO: error check for card not found
        if(cardIndex == -1){
            FileLogger.logError("DummyCardCommand error: " + card);
            FileLogger.logError("card not found: " + card + " id: " + card.getMetricID());
            FileLogger.logError("hand: " + AbstractDungeon.player.hand.group);
            valid = false;
            return null;
        }

        if(card.costForTurn > EnergyPanel.totalCount){
            FileLogger.logError("DummyCardCommand error: " + card);
            FileLogger.logError("Too expensive! " + card.costForTurn +" > "+ EnergyPanel.totalCount);
            return null;
        }

        FileLogger.log("hello enemyIndex: "+enemyIndex);
        if(enemyIndex != -1){
            List<AbstractMonster> targets = CardAction.getValidTargets(card);
            boolean targetValid = false;
            for (AbstractMonster m : targets) {
                if (enemyIndex == AbstractDungeon.getMonsters().monsters.indexOf(m)) {
                    targetValid = true;
                    break;
                }
            }
            if(!targetValid) {
                FileLogger.logError("DummyCardCommand error: " + card);
                FileLogger.logError("Target not found, index: " + enemyIndex);
                return null;
            }
        }

        return new CardCommand(cardIndex, enemyIndex, card.cardID + " " + card.name);

    }

    public boolean canRevalidate() {
        return valid;
    }
}
