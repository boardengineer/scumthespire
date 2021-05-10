package battleaimod.battleai.commands;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class GridSelectCommand implements Command {
    private final int cardIndex;

    public GridSelectCommand(int cardIndex) {
        this.cardIndex = cardIndex;
    }

    public GridSelectCommand(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.cardIndex = parsed.get("card_index").getAsInt();
    }

    @Override
    public void execute() {
//        System.err.println("select grid " + cardIndex);

        AbstractCard target = AbstractDungeon.gridSelectScreen.targetGroup.group.get(cardIndex);
        ReflectionHacks.setPrivate(
                AbstractDungeon.gridSelectScreen,
                GridCardSelectScreen.class,
                "hoveredCard",
                target);
        target.hb.clicked = true;

        AbstractDungeon.gridSelectScreen.update();

        if (!shouldGoFast()) {
            AbstractDungeon.actionManager.addToBottom(new WaitAction(.2F));
        } else {
            AbstractDungeon.actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;
        }
    }

    @Override
    public String encode() {
        JsonObject cardCommandJson = new JsonObject();

        cardCommandJson.addProperty("type", "GRID_SELECT");
        cardCommandJson.addProperty("card_index", cardIndex);


        return cardCommandJson.toString();
    }

    @Override
    public String toString() {
        return "GridSelectCommand" + cardIndex;
    }
}
