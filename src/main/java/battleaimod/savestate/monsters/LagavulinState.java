package battleaimod.savestate.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Lagavulin;

public class LagavulinState extends MonsterState {
    private final int debuffTurnCount;
    private final int idleCount;
    protected final boolean asleep;
    private final boolean isOut;
    private final boolean isOutTriggered;

    public LagavulinState(AbstractMonster monster) {
        super(monster);

        asleep = ReflectionHacks
                .getPrivate(monster, Lagavulin.class, "asleep");
        debuffTurnCount = ReflectionHacks
                .getPrivate(monster, Lagavulin.class, "debuffTurnCount");
        idleCount = ReflectionHacks
                .getPrivate(monster, Lagavulin.class, "idleCount");
        isOut = ReflectionHacks
                .getPrivate(monster, Lagavulin.class, "isOut");
        isOutTriggered = ReflectionHacks
                .getPrivate(monster, Lagavulin.class, "isOutTriggered");

        monsterTypeNumber = Monster.LAGAVULIN.ordinal();
    }

    public LagavulinState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.debuffTurnCount = parsed.get("debuff_turn_count").getAsInt();
        this.idleCount = parsed.get("idle_count").getAsInt();
        this.asleep = parsed.get("asleep").getAsBoolean();
        this.isOut = parsed.get("is_out").getAsBoolean();
        this.isOutTriggered = parsed.get("is_out_triggered").getAsBoolean();

        monsterTypeNumber = Monster.LAGAVULIN.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Lagavulin monster = new Lagavulin(asleep);
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, Lagavulin.class, "debuffTurnCount", debuffTurnCount);
        ReflectionHacks
                .setPrivate(monster, Lagavulin.class, "idleCount", idleCount);
        ReflectionHacks
                .setPrivate(monster, Lagavulin.class, "asleep", asleep);
        ReflectionHacks
                .setPrivate(monster, Lagavulin.class, "isOut", isOut);
        ReflectionHacks
                .setPrivate(monster, Lagavulin.class, "isOutTriggered", isOutTriggered);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("asleep", asleep);
        monsterStateJson.addProperty("is_out", isOut);
        monsterStateJson.addProperty("idle_count", idleCount);
        monsterStateJson.addProperty("debuff_turn_count", debuffTurnCount);
        monsterStateJson.addProperty("is_out_triggered", isOutTriggered);

        return monsterStateJson.toString();
    }
}
