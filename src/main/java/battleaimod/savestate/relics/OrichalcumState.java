package battleaimod.savestate.relics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Orichalcum;

public class OrichalcumState extends RelicState {
    private final boolean trigger;

    public OrichalcumState(AbstractRelic relic) {
        super(relic);

        this.trigger = ((Orichalcum) relic).trigger;
    }

    public OrichalcumState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.trigger = parsed.get("trigger").getAsBoolean();
    }

    @Override
    public AbstractRelic loadRelic() {
        Orichalcum result = (Orichalcum) super.loadRelic();

        result.trigger = trigger;

        return result;
    }

    @Override
    public String encode() {
        JsonObject result = new JsonParser().parse(super.encode()).getAsJsonObject();

        result.addProperty("trigger", trigger);

        return result.toString();
    }
}
