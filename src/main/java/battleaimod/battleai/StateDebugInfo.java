package battleaimod.battleai;

import battleaimod.savestate.SaveState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StateDebugInfo {
    private final int playerHealth;
    private final int monsterHealth;

    private final int numBurns;

    public StateDebugInfo(SaveState saveState) {
        playerHealth = saveState.getPlayerHealth();
        monsterHealth = saveState.curMapNodeState.monsterData.stream()
                                                             .map(monster -> monster.currentHealth)
                                                             .reduce(Integer::sum)
                                                             .get();
        numBurns = saveState.getNumInstances("Dazed");
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
