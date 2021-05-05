package battleaimod.savestate.monsters.exordium;

import basemod.ReflectionHacks;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Cultist;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class CultistState extends MonsterState {
    private final boolean firstMove;

    public CultistState(AbstractMonster monster) {
        super(monster);

        firstMove = ReflectionHacks
                .getPrivate(monster, Cultist.class, "firstMove");

        monsterTypeNumber = Monster.CULTIST.ordinal();
    }

    public CultistState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.firstMove = parsed.get("first_move").getAsBoolean();

        monsterTypeNumber = Monster.CULTIST.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Cultist monster = new Cultist(offsetX, offsetY);

        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, Cultist.class, "firstMove", firstMove);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("first_move", firstMove);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Cultist.class,
            paramtypez = {float.class, float.class, boolean.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {

        @SpireInsertPatch(loc = 67)
        public static SpireReturn Insert(Cultist _instance, float x, float y, boolean talk) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = Cultist.class,
            paramtypez = {},
            method = "die"
    )
    public static class CultistsDeathAnimationPatch {
        public static SpireReturn Prefix(Cultist _instance) {
            if (shouldGoFast()) {
                _instance.deathTimer = 0;
                _instance.die(true);
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
