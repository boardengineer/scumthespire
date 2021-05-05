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
import com.megacrit.cardcrawl.monsters.exordium.Looter;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class LooterState extends MonsterState {
    private final int slashCount;
    private final int stolenGold;

    public LooterState(AbstractMonster monster) {
        super(monster);

        slashCount = ReflectionHacks
                .getPrivate(monster, Looter.class, "slashCount");
        stolenGold = ReflectionHacks
                .getPrivate(monster, Looter.class, "stolenGold");

        monsterTypeNumber = Monster.LOOTER.ordinal();
    }

    public LooterState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.slashCount = parsed.get("slash_count").getAsInt();
        this.stolenGold = parsed.get("stolen_gold").getAsInt();

        monsterTypeNumber = Monster.LOOTER.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Looter monster = new Looter(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, Looter.class, "slashCount", slashCount);
        ReflectionHacks
                .setPrivate(monster, Looter.class, "stolenGold", stolenGold);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("slash_count", slashCount);
        monsterStateJson.addProperty("stolen_gold", stolenGold);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Looter.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 73)
        public static SpireReturn Looter(Looter _instance, float x, float y) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
