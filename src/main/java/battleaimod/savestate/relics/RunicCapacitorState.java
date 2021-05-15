package battleaimod.savestate.relics;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.RunicCapacitor;

public class RunicCapacitorState extends RelicState {
    private final boolean firstTurn;

    public RunicCapacitorState(AbstractRelic relic) {
        super(relic);

        this.firstTurn = ReflectionHacks
                .getPrivate(relic, RunicCapacitor.class, "firstTurn");
    }

    public RunicCapacitorState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.firstTurn = parsed.get("first_turn").getAsBoolean();
    }

    @Override
    public AbstractRelic loadRelic() {
        RunicCapacitor result = (RunicCapacitor) super.loadRelic();

        ReflectionHacks
                .setPrivate(result, RunicCapacitor.class, "firstTurn", firstTurn);

        return result;
    }

    @Override
    public String encode() {
        JsonObject parsed = new JsonParser().parse(super.encode()).getAsJsonObject();

        parsed.addProperty("first_turn", firstTurn);

        return parsed.toString();
    }
}
