package battleaimod.savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;

public class RelicState {
    private final String relicId;
    private final int counter;

    public RelicState(AbstractRelic relic) {
        this.relicId = relic.relicId;
        this.counter = relic.counter;
    }

    public RelicState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.relicId = parsed.get("relic_id").getAsString();
        this.counter = parsed.get("counter").getAsInt();
    }

    public AbstractRelic loadRelic() {
        AbstractRelic result = RelicLibrary.getRelic(relicId).makeCopy();
        result.counter = counter;
        return result;
    }

    public String encode() {
        JsonObject relicStateJson = new JsonObject();

        relicStateJson.addProperty("relic_id", relicId);
        relicStateJson.addProperty("counter", counter);

        return relicStateJson.toString();
    }
}