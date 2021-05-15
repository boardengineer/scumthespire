package battleaimod.savestate.relics;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Necronomicon;

public class NecronomiconState extends RelicState {
    private final boolean activated;

    public NecronomiconState(AbstractRelic relic) {
        super(relic);

        this.activated = ReflectionHacks
                .getPrivate(relic, Necronomicon.class, "activated");
    }

    public NecronomiconState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.activated = parsed.get("activated").getAsBoolean();
    }

    @Override
    public AbstractRelic loadRelic() {
        Necronomicon relic = (Necronomicon) super.loadRelic();

        ReflectionHacks
                .setPrivate(relic, Necronomicon.class, "activated", activated);

        return relic;
    }

    @Override
    public String encode() {
        JsonObject parsed = new JsonParser().parse(super.encode()).getAsJsonObject();

        parsed.addProperty("activated", activated);

        return parsed.toString();
    }
}
