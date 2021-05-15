package battleaimod.savestate.relics;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.GamblingChip;

public class GamblingChipState extends RelicState {
    private final boolean activated;

    public GamblingChipState(AbstractRelic relic) {
        super(relic);

        this.activated = ReflectionHacks
                .getPrivate(relic, GamblingChip.class, "activated");
    }

    public GamblingChipState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.activated = parsed.get("activated").getAsBoolean();
    }

    @Override
    public AbstractRelic loadRelic() {
        GamblingChip result = (GamblingChip) super.loadRelic();

        ReflectionHacks
                .setPrivate(result, GamblingChip.class, "activated", activated);

        return result;
    }

    @Override
    public String encode() {
        JsonObject parsed = new JsonParser().parse(super.encode()).getAsJsonObject();

        parsed.addProperty("activated", activated);

        return parsed.toString();
    }
}
