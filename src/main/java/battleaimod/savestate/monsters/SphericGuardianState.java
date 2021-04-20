package battleaimod.savestate.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.SphericGuardian;

public class SphericGuardianState extends MonsterState {
    private final boolean firstMove;
    private final boolean secondMove;

    public SphericGuardianState(AbstractMonster monster) {
        super(monster);

        firstMove = ReflectionHacks
                .getPrivate(monster, SphericGuardian.class, "firstMove");
        secondMove = ReflectionHacks
                .getPrivate(monster, SphericGuardian.class, "secondMove");

        monsterTypeNumber = Monster.SPHERIC_GUARDIAN.ordinal();
    }

    public SphericGuardianState(String jsonString) {
        super(jsonString);


        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.firstMove = parsed.get("first_move").getAsBoolean();
        this.secondMove = parsed.get("second_move").getAsBoolean();

        monsterTypeNumber = Monster.SPHERIC_GUARDIAN.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SphericGuardian monster = new SphericGuardian(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, SphericGuardian.class, "firstMove", firstMove);
        ReflectionHacks
                .setPrivate(monster, SphericGuardian.class, "secondMove", secondMove);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("first_move", firstMove);
        monsterStateJson.addProperty("second_move", secondMove);

        return monsterStateJson.toString();
    }
}
