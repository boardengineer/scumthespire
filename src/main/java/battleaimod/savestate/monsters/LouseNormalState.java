package battleaimod.savestate.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.LouseNormal;

public class LouseNormalState extends MonsterState {
    private final boolean isOpen;
    private final int biteDamage;

    public LouseNormalState(AbstractMonster monster) {
        super(monster);

        isOpen = ReflectionHacks
                .getPrivate(monster, LouseNormal.class, "isOpen");
        biteDamage = ReflectionHacks
                .getPrivate(monster, LouseNormal.class, "biteDamage");

        monsterTypeNumber = Monster.FUZZY_LOUSE_NORMAL.ordinal();
    }

    public LouseNormalState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.isOpen = parsed.get("is_open").getAsBoolean();
        this.biteDamage = parsed.get("bite_damage").getAsInt();

        monsterTypeNumber = Monster.FUZZY_LOUSE_NORMAL.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        LouseNormal monster = new LouseNormal(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, LouseNormal.class, "isOpen", isOpen);
        ReflectionHacks
                .setPrivate(monster, LouseNormal.class, "biteDamage", biteDamage);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("is_open", isOpen);
        monsterStateJson.addProperty("bite_damage", biteDamage);

        return monsterStateJson.toString();
    }
}
