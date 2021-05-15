package battleaimod.savestate.relics;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.UnceasingTop;

public class UnceasingTopState extends RelicState {
    private final boolean disabledUntilEndOfTurn;
    private final boolean canDraw;

    public UnceasingTopState(AbstractRelic relic) {
        super(relic);

        this.disabledUntilEndOfTurn = ReflectionHacks
                .getPrivate(relic, UnceasingTop.class, "disabledUntilEndOfTurn");
        this.canDraw = ReflectionHacks
                .getPrivate(relic, UnceasingTop.class, "canDraw");
    }

    public UnceasingTopState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.disabledUntilEndOfTurn = parsed.get("disabled_until_end_of_turn").getAsBoolean();
        this.canDraw = parsed.get("can_draw").getAsBoolean();
    }

    @Override
    public AbstractRelic loadRelic() {
        UnceasingTop result = (UnceasingTop) super.loadRelic();

        ReflectionHacks
                .setPrivate(result, UnceasingTop.class, "canDraw", canDraw);
        ReflectionHacks
                .setPrivate(result, UnceasingTop.class, "disabledUntilEndOfTurn", disabledUntilEndOfTurn);

        return result;
    }

    @Override
    public String encode() {
        JsonObject parsed = new JsonParser().parse(super.encode()).getAsJsonObject();

        parsed.addProperty("can_draw", canDraw);
        parsed.addProperty("disabled_until_end_of_turn", disabledUntilEndOfTurn);

        return parsed.toString();
    }
}
