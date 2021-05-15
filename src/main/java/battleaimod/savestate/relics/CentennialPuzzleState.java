package battleaimod.savestate.relics;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.CentennialPuzzle;

public class CentennialPuzzleState extends RelicState {
    private final boolean usedThisCombat;

    public CentennialPuzzleState(AbstractRelic relic) {
        super(relic);

        this.usedThisCombat = ReflectionHacks
                .getPrivate(relic, CentennialPuzzle.class, "usedThisCombat");
    }

    public CentennialPuzzleState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.usedThisCombat = parsed.get("used_this_combat").getAsBoolean();
    }

    @Override
    public AbstractRelic loadRelic() {
        CentennialPuzzle result = (CentennialPuzzle) super.loadRelic();

        ReflectionHacks
                .setPrivate(result, CentennialPuzzle.class, "usedThisCombat", usedThisCombat);

        return result;
    }

    @Override
    public String encode() {
        JsonObject parsed = new JsonParser().parse(super.encode()).getAsJsonObject();

        parsed.addProperty("used_this_combat", usedThisCombat);

        return parsed.toString();
    }
}
