package battleai;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.DiscardAtEndOfTurnAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import communicationmod.GameStateListener;
import org.apache.logging.log4j.LogManager;

import java.util.Iterator;

public class EndCommand implements Command {
    @Override
    public void execute() {
        endTurn();
    }

    private void endTurn() {
        GameStateListener.signalTurnEnd();

        AbstractDungeon.player.applyEndOfTurnTriggers();
        AbstractDungeon.actionManager.addToBottom(new DiscardAtEndOfTurnAction());
        Iterator var1 = AbstractDungeon.player.drawPile.group.iterator();

        AbstractCard c;
        while (var1.hasNext()) {
            c = (AbstractCard) var1.next();
            c.resetAttributes();
        }

        var1 = AbstractDungeon.player.discardPile.group.iterator();

        while (var1.hasNext()) {
            c = (AbstractCard) var1.next();
            c.resetAttributes();
        }

        var1 = AbstractDungeon.player.hand.group.iterator();

        while (var1.hasNext()) {
            c = (AbstractCard) var1.next();
            c.resetAttributes();
        }

        if (AbstractDungeon.player.hoveredCard != null) {
            AbstractDungeon.player.hoveredCard.resetAttributes();
        }


        LogManager.getLogger().info("string action manager");

        System.out.println(AbstractDungeon.actionManager.actions);
        AbstractDungeon.actionManager.addToBottom(new SomeAction());
        AbstractDungeon.player.isEndingTurn = false;
    }

    public static class SomeAction extends AbstractGameAction {

        @Override
        public void update() {
            AbstractDungeon.actionManager.turnHasEnded = true;
            GameActionManager.playerHpLastTurn = AbstractDungeon.player.currentHealth;
            AbstractDungeon.actionManager.monsterAttacksQueued = false;
            this.isDone = true;
        }
    }
}
