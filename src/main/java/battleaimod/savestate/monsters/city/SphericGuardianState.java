package battleaimod.savestate.monsters.city;

import basemod.ReflectionHacks;
import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.SphericGuardian;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

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

    @SpirePatch(
            clz = SphericGuardian.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 54)
        public static SpireReturn SphericGuardian(SphericGuardian _instance, float x, float y) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
