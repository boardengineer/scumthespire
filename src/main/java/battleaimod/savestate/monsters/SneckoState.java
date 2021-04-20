package battleaimod.savestate.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Snecko;

public class SneckoState extends MonsterState {
    private final boolean firstTurn;

    public SneckoState(AbstractMonster monster) {
        super(monster);

        firstTurn = ReflectionHacks
                .getPrivate(monster, Snecko.class, "firstTurn");

        monsterTypeNumber = Monster.SNECKO.ordinal();
    }

    public SneckoState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.firstTurn = parsed.get("first_turn").getAsBoolean();

        monsterTypeNumber = Monster.SNECKO.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Snecko monster = new Snecko(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, Snecko.class, "firstTurn", firstTurn);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("first_turn", firstTurn);

        return monsterStateJson.toString();
    }
}
