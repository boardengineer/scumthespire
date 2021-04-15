package battleaimod.savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Orichalcum;

public class RelicState {
    private final String relicId;
    private final int counter;
    private final boolean grayscale;

    private final boolean orichalcumTrigger;

    public RelicState(AbstractRelic relic) {
        this.relicId = relic.relicId;
        this.counter = relic.counter;
        this.grayscale = relic.grayscale;

        if (relic instanceof Orichalcum) {
            this.orichalcumTrigger = ((Orichalcum) relic).trigger;
        } else {
            orichalcumTrigger = false;
        }
    }

    public RelicState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.relicId = parsed.get("relic_id").getAsString();
        this.counter = parsed.get("counter").getAsInt();
        this.grayscale = parsed.get("grayscale").getAsBoolean();

        this.orichalcumTrigger = parsed.get("orichalcum_trigger").getAsBoolean();

    }

    public AbstractRelic loadRelic() {
        AbstractRelic result = RelicLibrary.getRelic(relicId).makeCopy();
        result.counter = counter;
        result.grayscale = grayscale;

        if (result instanceof Orichalcum) {
            ((Orichalcum) result).trigger = orichalcumTrigger;
        }

        return result;
    }

    public String encode() {
        JsonObject relicStateJson = new JsonObject();

        relicStateJson.addProperty("relic_id", relicId);
        relicStateJson.addProperty("counter", counter);
        relicStateJson.addProperty("grayscale", grayscale);

        relicStateJson.addProperty("orichalcum_trigger", orichalcumTrigger);

        return relicStateJson.toString();
    }
}
