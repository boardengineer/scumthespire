package battleaimod.savestate.relics;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.HoveringKite;

public class HoveringKiteState extends RelicState {
    private final boolean triggeredThisTurn;

    public HoveringKiteState(AbstractRelic relic) {
        super(relic);

        this.triggeredThisTurn = ReflectionHacks
                .getPrivate(relic, HoveringKite.class, "triggeredThisTurn");
    }

    public HoveringKiteState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.triggeredThisTurn = parsed.get("triggered_this_turn").getAsBoolean();
    }

    @Override
    public AbstractRelic loadRelic() {
        HoveringKite result = (HoveringKite) super.loadRelic();

        ReflectionHacks
                .setPrivate(result, HoveringKite.class, "triggeredThisTurn", triggeredThisTurn);

        return result;
    }

    @Override
    public String encode() {
        JsonObject parsed = new JsonParser().parse(super.encode()).getAsJsonObject();

        parsed.addProperty("triggered_this_turn", triggeredThisTurn);

        return parsed.toString();
    }
}
