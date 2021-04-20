package battleaimod.savestate.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.LouseDefensive;

public class LouseDefensiveState extends MonsterState {
    private final boolean isOpen;
    private final int biteDamage;

    public LouseDefensiveState(AbstractMonster monster) {
        super(monster);

        isOpen = ReflectionHacks
                .getPrivate(monster, LouseDefensive.class, "isOpen");
        biteDamage = ReflectionHacks
                .getPrivate(monster, LouseDefensive.class, "biteDamage");

        monsterTypeNumber = Monster.FUZZY_LOUSE_DEFENSIVE.ordinal();
    }

    public LouseDefensiveState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.isOpen = parsed.get("is_open").getAsBoolean();
        this.biteDamage = parsed.get("bite_damage").getAsInt();

        monsterTypeNumber = Monster.FUZZY_LOUSE_DEFENSIVE.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        LouseDefensive monster = new LouseDefensive(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, LouseDefensive.class, "isOpen", isOpen);
        ReflectionHacks
                .setPrivate(monster, LouseDefensive.class, "biteDamage", biteDamage);


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
