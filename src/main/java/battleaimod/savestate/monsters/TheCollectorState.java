package battleaimod.savestate.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.TheCollector;

public class TheCollectorState extends MonsterState {
    private final int turnsTaken;
    private final boolean utUsed;
    private final boolean initialSpawn;

    public TheCollectorState(AbstractMonster monster) {
        super(monster);

        this.initialSpawn = ReflectionHacks
                .getPrivate(monster, TheCollector.class, "initialSpawn");
        this.turnsTaken = ReflectionHacks
                .getPrivate(monster, TheCollector.class, "turnsTaken");
        this.utUsed = ReflectionHacks
                .getPrivate(monster, TheCollector.class, "ultUsed");

        this.monsterTypeNumber = Monster.THE_COLLECTOR.ordinal();
    }

    public TheCollectorState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.utUsed = parsed.get("ult_used").getAsBoolean();
        this.turnsTaken = parsed.get("turns_taken").getAsInt();
        this.initialSpawn = parsed.get("initial_spawn").getAsBoolean();

        monsterTypeNumber = Monster.THE_COLLECTOR.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        TheCollector monster = new TheCollector();
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, TheCollector.class, "initialSpawn", initialSpawn);
        ReflectionHacks
                .setPrivate(monster, TheCollector.class, "turnsTaken", turnsTaken);
        ReflectionHacks
                .setPrivate(monster, TheCollector.class, "ultUsed", utUsed);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("initial_spawn", initialSpawn);
        monsterStateJson.addProperty("turns_taken", turnsTaken);
        monsterStateJson.addProperty("ult_used", utUsed);

        return monsterStateJson.toString();
    }
}
