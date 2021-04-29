package battleaimod.battleai.commands;

import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class HandSelectConfirmCommand implements Command {
    public static final HandSelectConfirmCommand INSTANCE = new HandSelectConfirmCommand();

    private HandSelectConfirmCommand() {
    }

    @Override
    public void execute() {
        AbstractDungeon.handCardSelectScreen.button.hb.clicked = true;
        AbstractDungeon.handCardSelectScreen.update();
    }

    @Override
    public String encode() {
        JsonObject cardCommandJson = new JsonObject();

        cardCommandJson.addProperty("type", "HAND_SELECT_CONFIRM");

        return cardCommandJson.toString();
    }
}
