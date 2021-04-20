package battleaimod.savestate.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.ShelledParasite;

public class ShelledParasiteState extends MonsterState {
    private final boolean firstMove;

    public ShelledParasiteState(AbstractMonster monster) {
        super(monster);

        firstMove = ReflectionHacks
                .getPrivate(monster, ShelledParasite.class, "firstMove");

        monsterTypeNumber = Monster.SHELLED_PARASITE.ordinal();
    }

    public ShelledParasiteState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.firstMove = parsed.get("first_move").getAsBoolean();

        monsterTypeNumber = Monster.SHELLED_PARASITE.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        ShelledParasite monster = new ShelledParasite(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, ShelledParasite.class, "firstMove", firstMove);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("first_move", firstMove);

        return monsterStateJson.toString();
    }
}
