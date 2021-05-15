package battleaimod.savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.EventHelper;
import com.megacrit.cardcrawl.random.Random;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores all RNG counters for the game as well as event chances.
 */
public class RngState {
    private final Random monsterRng;
    private final Random mapRng;
    private final Random eventRng;
    private final Random merchantRng;
    private final Random cardRng;
    private final Random treasureRng;
    private final Random relicRng;
    private final Random potionRng;
    private final Random monsterHpRng;
    private final Random aiRng;
    private final Random shuffleRng;
    private final Random cardRandomRng;
    private final Random miscRng;
    private final long seed;

    private final ArrayList<Float> eventHelperChances;

    public RngState() {
        seed = Settings.seed;
        monsterRng = new Random(Settings.seed, AbstractDungeon.monsterRng.counter);
        mapRng = new Random(Settings.seed, AbstractDungeon.mapRng.counter);
        eventRng = new Random(Settings.seed, AbstractDungeon.eventRng.counter);
        merchantRng = new Random(Settings.seed, AbstractDungeon.merchantRng.counter);
        cardRng = new Random(Settings.seed, AbstractDungeon.cardRng.counter);
        treasureRng = new Random(Settings.seed, AbstractDungeon.treasureRng.counter);
        relicRng = new Random(Settings.seed, AbstractDungeon.relicRng.counter);
        potionRng = new Random(Settings.seed, AbstractDungeon.potionRng.counter);
        monsterHpRng = new Random(Settings.seed, AbstractDungeon.monsterHpRng.counter);
        aiRng = new Random(Settings.seed, AbstractDungeon.aiRng.counter);
        shuffleRng = new Random(Settings.seed, AbstractDungeon.shuffleRng.counter);
        cardRandomRng = new Random(Settings.seed, AbstractDungeon.cardRandomRng.counter);
        miscRng = new Random(Settings.seed, AbstractDungeon.miscRng.counter);
        eventHelperChances = EventHelper.getChances();
    }

    public RngState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        seed = parsed.get("seed").getAsLong();
        monsterRng = new Random(seed, parsed.get("monster_rng_counter").getAsInt());
        mapRng = new Random(seed, parsed.get("map_rng_counter").getAsInt());
        eventRng = new Random(seed, parsed.get("event_rng_counter").getAsInt());
        merchantRng = new Random(seed, parsed.get("merchant_rng_counter").getAsInt());
        cardRng = new Random(seed, parsed.get("card_rng_counter").getAsInt());
        treasureRng = new Random(seed, parsed.get("treasure_rng_counter").getAsInt());
        relicRng = new Random(seed, parsed.get("relic_rng_counter").getAsInt());
        potionRng = new Random(seed, parsed.get("potion_rng_counter").getAsInt());
        monsterHpRng = new Random(seed, parsed.get("monster_hp_rng_counter").getAsInt());
        aiRng = new Random(seed, parsed.get("ai_rng_counter").getAsInt());
        shuffleRng = new Random(seed, parsed.get("shuffle_rng_counter").getAsInt());
        cardRandomRng = new Random(seed, parsed.get("card_random_rng_counter").getAsInt());
        miscRng = new Random(seed, parsed.get("misc_rng_counter").getAsInt());

        String helperString = parsed.get("event_helper_chances").getAsString();
        eventHelperChances = Stream.of(helperString.split(",")).map(Float::parseFloat)
                                   .collect(Collectors.toCollection(ArrayList::new));
    }

    public void loadRng() {
        Settings.seed = seed;
        AbstractDungeon.monsterRng = new Random(Settings.seed, monsterRng.counter);
        AbstractDungeon.mapRng = new Random(Settings.seed, mapRng.counter);
        AbstractDungeon.eventRng = new Random(Settings.seed, eventRng.counter);
        AbstractDungeon.merchantRng = new Random(Settings.seed, merchantRng.counter);
        AbstractDungeon.cardRng = new Random(Settings.seed, cardRng.counter);
        AbstractDungeon.treasureRng = new Random(Settings.seed, treasureRng.counter);
        AbstractDungeon.relicRng = new Random(Settings.seed, relicRng.counter);
        AbstractDungeon.potionRng = new Random(Settings.seed, potionRng.counter);
        AbstractDungeon.monsterHpRng = new Random(Settings.seed, monsterHpRng.counter);
        AbstractDungeon.aiRng = new Random(Settings.seed, aiRng.counter);
        AbstractDungeon.shuffleRng = new Random(Settings.seed, shuffleRng.counter);
        AbstractDungeon.cardRandomRng = new Random(Settings.seed, cardRandomRng.counter);
        AbstractDungeon.miscRng = new Random(Settings.seed, miscRng.counter);
        EventHelper.setChances(eventHelperChances);
    }

    public String encode() {
        JsonObject rngStateJson = new JsonObject();

        rngStateJson.addProperty("seed", seed);
        rngStateJson.addProperty("monster_rng_counter", monsterRng.counter);
        rngStateJson.addProperty("map_rng_counter", mapRng.counter);
        rngStateJson.addProperty("event_rng_counter", eventRng.counter);
        rngStateJson.addProperty("merchant_rng_counter", merchantRng.counter);
        rngStateJson.addProperty("card_rng_counter", cardRng.counter);
        rngStateJson.addProperty("treasure_rng_counter", treasureRng.counter);
        rngStateJson.addProperty("relic_rng_counter", relicRng.counter);
        rngStateJson.addProperty("potion_rng_counter", potionRng.counter);
        rngStateJson.addProperty("monster_hp_rng_counter", monsterHpRng.counter);
        rngStateJson.addProperty("ai_rng_counter", aiRng.counter);
        rngStateJson.addProperty("shuffle_rng_counter", shuffleRng.counter);
        rngStateJson.addProperty("card_random_rng_counter", cardRandomRng.counter);
        rngStateJson.addProperty("misc_rng_counter", miscRng.counter);

        String helperString = eventHelperChances.stream().map(f -> Float.toString(f))
                                                .collect(Collectors.joining(","));

        rngStateJson.addProperty("event_helper_chances", helperString);

        return rngStateJson.toString();
    }

}
