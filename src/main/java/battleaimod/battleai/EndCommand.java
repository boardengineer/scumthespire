package battleaimod.battleai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class EndCommand implements Command {
    public StateDebugInfo stateDebugInfo = null;

    public EndCommand() {
    }

    public EndCommand(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        stateDebugInfo = new StateDebugInfo(parsed.get("state_debug_info").getAsString());
    }

    @Override
    public void execute() {
        AbstractDungeon.overlayMenu.endTurnButton.disable(true);
    }

    @Override
    public String toString() {
        return "EndCommand{" +
                "stateDebugInfo = " + stateDebugInfo.encode() +
                '}';
    }

    @Override
    public String encode() {
        JsonObject endCommandJson = new JsonObject();

        endCommandJson.addProperty("type", "END");

        if (stateDebugInfo != null) {
            endCommandJson.addProperty("state_debug_info", stateDebugInfo.encode());
        }
        return endCommandJson.toString();
    }
}
