package battleaimod.savestate.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Chosen;

public class ChosenState extends MonsterState {
    private final boolean firstTurn;
    private final boolean usedHex;

    public ChosenState(AbstractMonster monster) {
        super(monster);

        firstTurn = ReflectionHacks
                .getPrivate(monster, Chosen.class, "firstTurn");
        usedHex = ReflectionHacks
                .getPrivate(monster, Chosen.class, "usedHex");

        monsterTypeNumber = Monster.CHOSEN.ordinal();
    }

    public ChosenState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.usedHex = parsed.get("used_hex").getAsBoolean();
        this.firstTurn = parsed.get("first_turn").getAsBoolean();

        monsterTypeNumber = Monster.CHOSEN.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Chosen monster = new Chosen(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, Chosen.class, "usedHex", usedHex);
        ReflectionHacks.setPrivate(monster, Chosen.class, "firstTurn", firstTurn);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("used_hex", usedHex);
        monsterStateJson.addProperty("first_turn", firstTurn);

        return monsterStateJson.toString();
    }
}
