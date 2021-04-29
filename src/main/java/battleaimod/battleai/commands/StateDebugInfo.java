package battleaimod.battleai.commands;

import battleaimod.savestate.SaveState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StateDebugInfo {
    private final int playerHealth;
    private final int monsterHealth;

    private final int numBurns;

    public StateDebugInfo(SaveState saveState) {
        int upgradedCard = 0;

        upgradedCard += saveState.playerState.hand.stream().map(cardState -> cardState.upgraded ? 1 : 0).reduce(Integer::sum).orElse(0);
        upgradedCard += saveState.playerState.discardPile.stream().map(cardState -> cardState.upgraded ? 1 : 0).reduce(Integer::sum).orElse(0);
        upgradedCard += saveState.playerState.drawPile.stream().map(cardState -> cardState.upgraded ? 1 : 0).reduce(Integer::sum).orElse(0);

        int totalCards = 0;

        totalCards += saveState.playerState.hand.size();
        totalCards +=saveState.playerState.discardPile.size();
        totalCards += saveState.playerState.drawPile.size();

        int numArmaments = 0;

        numArmaments += saveState.playerState.hand.stream().map(cardState -> cardState.cardId.equals("Strike_R") ? 1 : 0).reduce(Integer::sum).orElse(0);
        numArmaments += saveState.playerState.discardPile.stream().map(cardState -> cardState.cardId.equals("Strike_R") ? 1 : 0).reduce(Integer::sum).orElse(0);
        numArmaments += saveState.playerState.drawPile.stream().map(cardState -> cardState.cardId.equals("Strike_R") ? 1 : 0).reduce(Integer::sum).orElse(0);


        this.playerHealth = upgradedCard;
        this.monsterHealth = totalCards;
        this.numBurns = numArmaments;
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
