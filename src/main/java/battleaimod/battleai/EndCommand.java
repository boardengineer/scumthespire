package battleaimod.battleai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class EndCommand implements Command {
    public StateDebugInfo stateDebugInfo = null;

    public EndCommand() {
    }

    public EndCommand(String jsonString) {
        System.err.println("Here 1");
        try {
            System.err.println("Here 2");
            JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

            System.err.println("Here 3");
            if (parsed.has("state_debug_info"))
                stateDebugInfo = new StateDebugInfo(parsed.get("state_debug_info").getAsString());
        } catch (Exception e) {
            System.err.println("Exception");
            // still return a plain End Command
        }

    }

    @Override
    public void execute() {
//        System.err.println("executing end");

        AbstractDungeon.overlayMenu.endTurnButton.disable(true);
    }

    @Override
    public String toString() {
        return "EndCommand  ";
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
