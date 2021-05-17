package battleaimod.battleai.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import savestate.SaveState;

public class StateDebugInfo {
    private final int playerHealth;
    private final int monsterHealth;

    private final int numBurns;

    public StateDebugInfo(SaveState saveState) {
        this.playerHealth = saveState.getPlayerHealth();
        this.monsterHealth = saveState.curMapNodeState.monsterData.stream()
                                                                  .map(monster -> monster.currentHealth)
                                                                  .reduce(Integer::sum).orElse(0);
        this.numBurns = 0;
    }

    public StateDebugInfo(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        playerHealth = parsed.get("player_health").getAsInt();
        monsterHealth = parsed.get("monster_health").getAsInt();

        numBurns = parsed.get("num_burns").getAsInt();
    }

    public String encode() {
        JsonObject stateDebugInfoJson = new JsonObject();

        stateDebugInfoJson.addProperty("player_health", playerHealth);
        stateDebugInfoJson.addProperty("monster_health", monsterHealth);

        stateDebugInfoJson.addProperty("num_burns", numBurns);

        return stateDebugInfoJson.toString();
    }
}
