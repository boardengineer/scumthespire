package battleaimod.savestate.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Champ;

public class ChampState extends MonsterState {
    private final int numTurns;
    private final int forgeTimes;
    private final int forgeThreshold;
    private final boolean thresholdReached;
    private final boolean firstTurn;

    public ChampState(AbstractMonster monster) {
        super(monster);

        numTurns = ReflectionHacks
                .getPrivate(monster, Champ.class, "numTurns");
        forgeTimes = ReflectionHacks
                .getPrivate(monster, Champ.class, "forgeTimes");
        forgeThreshold = ReflectionHacks
                .getPrivate(monster, Champ.class, "forgeThreshold");
        thresholdReached = ReflectionHacks
                .getPrivate(monster, Champ.class, "thresholdReached");
        firstTurn = ReflectionHacks
                .getPrivate(monster, Champ.class, "firstTurn");

        monsterTypeNumber = Monster.CHAMP.ordinal();
    }

    public ChampState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.numTurns = parsed.get("num_turns").getAsInt();
        this.forgeTimes = parsed.get("forge_times").getAsInt();
        this.forgeThreshold = parsed.get("forge_threshold").getAsInt();
        this.thresholdReached = parsed.get("threshold_reached").getAsBoolean();
        this.firstTurn = parsed.get("first_turn").getAsBoolean();

        monsterTypeNumber = Monster.CHAMP.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Champ monster = new Champ();
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, Champ.class, "numTurns", numTurns);
        ReflectionHacks
                .setPrivate(monster, Champ.class, "forgeTimes", forgeTimes);
        ReflectionHacks
                .setPrivate(monster, Champ.class, "forgeThreshold", forgeThreshold);
        ReflectionHacks
                .setPrivate(monster, Champ.class, "thresholdReached", thresholdReached);
        ReflectionHacks
                .setPrivate(monster, Champ.class, "firstTurn", firstTurn);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("num_turns", numTurns);
        monsterStateJson.addProperty("forge_times", forgeTimes);
        monsterStateJson.addProperty("forge_threshold", forgeThreshold);
        monsterStateJson.addProperty("threshold_reached", thresholdReached);
        monsterStateJson.addProperty("first_turn", firstTurn);

        return monsterStateJson.toString();
    }
}
