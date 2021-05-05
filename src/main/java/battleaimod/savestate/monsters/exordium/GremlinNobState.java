package battleaimod.savestate.monsters.exordium;

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
import com.megacrit.cardcrawl.monsters.exordium.GremlinNob;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class GremlinNobState extends MonsterState {
    private final boolean usedBellow;
    private final boolean canVuln;

    public GremlinNobState(AbstractMonster monster) {
        super(monster);

        this.usedBellow = ReflectionHacks.getPrivate(monster, GremlinNob.class, "usedBellow");
        this.canVuln = ReflectionHacks.getPrivate(monster, GremlinNob.class, "canVuln");

        monsterTypeNumber = Monster.GREMLIN_NOB.ordinal();
    }

    public GremlinNobState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.usedBellow = parsed.get("used_bellow").getAsBoolean();
        this.canVuln = parsed.get("can_vuln").getAsBoolean();

        monsterTypeNumber = Monster.GREMLIN_NOB.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinNob monster = new GremlinNob(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, GremlinNob.class, "usedBellow", usedBellow);
        ReflectionHacks.setPrivate(monster, GremlinNob.class, "canVuln", canVuln);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("can_vuln", canVuln);
        monsterStateJson.addProperty("used_bellow", usedBellow);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = GremlinNob.class,
            paramtypez = {float.class, float.class, boolean.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 70)
        public static SpireReturn GremlinNob(GremlinNob _instance, float x, float y, boolean setVuln) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
