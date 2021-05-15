package battleaimod.savestate.relics;

import basemod.ReflectionHacks;
import battleaimod.BattleAiMod;
import battleaimod.fastobjects.NoLoggerMummifiedHand;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.*;

public class RelicState {
    private final String relicId;
    private final int counter;
    private final boolean grayscale;
    private final boolean pulse;

    RelicState(AbstractRelic relic) {
        this.relicId = relic.relicId;
        this.counter = relic.counter;
        this.grayscale = relic.grayscale;
        this.pulse = ReflectionHacks.getPrivate(relic, AbstractRelic.class, "pulse");
    }

    RelicState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.relicId = parsed.get("relic_id").getAsString();
        this.counter = parsed.get("counter").getAsInt();
        this.grayscale = parsed.get("grayscale").getAsBoolean();
        this.pulse = parsed.get("pulse").getAsBoolean();
    }

    public AbstractRelic loadRelic() {
        AbstractRelic result;

        long makeRelicCopyStartTime = System.currentTimeMillis();

        if (relicId.equals("Mummified Hand")) {
            result = new NoLoggerMummifiedHand();
        } else {
            result = RelicLibrary.getRelic(relicId).makeCopy();
        }

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController.addRuntime("Load Time Relic Copy", System
                    .currentTimeMillis() - makeRelicCopyStartTime);
        }

        result.counter = counter;
        result.grayscale = grayscale;

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController.addRuntime("Load Time Load Relic", System
                    .currentTimeMillis() - makeRelicCopyStartTime);
        }

        ReflectionHacks.setPrivate(result, AbstractRelic.class, "pulse", pulse);

        return result;
    }

    public String encode() {
        JsonObject relicStateJson = new JsonObject();

        relicStateJson.addProperty("relic_id", relicId);
        relicStateJson.addProperty("counter", counter);
        relicStateJson.addProperty("grayscale", grayscale);
        relicStateJson.addProperty("pulse", pulse);

        return relicStateJson.toString();
    }

    public static RelicState forRelic(AbstractRelic relic) {
        if (BattleAiMod.relicByIdmap.containsKey(relic.relicId)) {
            return BattleAiMod.relicByIdmap.get(relic.relicId).factory.apply(relic);
        }

        return new RelicState(relic);
    }

    public static RelicState forJsonString(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        String relicId = parsed.get("relic_id").getAsString();
        if (BattleAiMod.relicByIdmap.containsKey(relicId)) {
            return BattleAiMod.relicByIdmap.get(relicId).jsonFactory.apply(jsonString);
        }

        return new RelicState(jsonString);
    }
}
