package battleaimod.battleai;

import battleaimod.GameStateListener;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.DiscardAtEndOfTurnAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import org.apache.logging.log4j.LogManager;

import java.util.Iterator;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class EndCommand implements Command {
    @Override
    public void execute() {
        System.err.println("executing end");
        if(shouldGoFast()) {
            endTurn();
        } else {
            AbstractDungeon.overlayMenu.endTurnButton.disable(true);
        }
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
        AbstractDungeon.actionManager.addToBottom(new CustonEndGameAction());
        AbstractDungeon.player.isEndingTurn = false;
    }

    public static class CustonEndGameAction extends AbstractGameAction {
        @Override
        public void update() {
            AbstractDungeon.actionManager.turnHasEnded = true;
            GameActionManager.playerHpLastTurn = AbstractDungeon.player.currentHealth;
            AbstractDungeon.actionManager.monsterAttacksQueued = false;
            this.isDone = true;
        }
    }

    @Override
    public String encode() {
        JsonObject endCommandJson = new JsonObject();

        endCommandJson.addProperty("type", "END");

        return endCommandJson.toString();
    }
}