package battleaimod.savestate.relics;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.RedSkull;

public class RedSkullState extends RelicState {
    private final boolean isActive;

    public RedSkullState(AbstractRelic relic) {
        super(relic);

        this.isActive = ReflectionHacks
                .getPrivate(relic, RedSkull.class, "isActive");
    }

    public RedSkullState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.isActive = parsed.get("is_active").getAsBoolean();
    }

    @Override
    public AbstractRelic loadRelic() {
        RedSkull result = (RedSkull) super.loadRelic();

        ReflectionHacks
                .setPrivate(result, RedSkull.class, "isActive", isActive);

        return result;
    }

    @Override
    public String encode() {
        JsonObject parsed = new JsonParser().parse(super.encode()).getAsJsonObject();

        parsed.addProperty("is_active", isActive);

        return parsed.toString();
    }
}
