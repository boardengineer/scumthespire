package battleaimod.battleai;

import battleaimod.savestate.SaveState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StateDebugInfo {
    private final int playerHealth;
    private final int monsterHealth;

    public StateDebugInfo(SaveState saveState) {
        playerHealth = saveState.getPlayerHealth();
        monsterHealth = saveState.curMapNodeState.monsterData.stream()
                                                             .map(monster -> monster.currentHealth)
                                                             .reduce(Integer::sum)
                                                             .get();
    }

    public StateDebugInfo(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        playerHealth = parsed.get("player_health").getAsInt();
        monsterHealth = parsed.get("monster_health").getAsInt();
    }

    public String encode() {
        JsonObject stateDebugInfoJson = new JsonObject();

        stateDebugInfoJson.addProperty("player_health", playerHealth);
        stateDebugInfoJson.addProperty("monster_health", monsterHealth);

        return stateDebugInfoJson.toString();
    }
}
