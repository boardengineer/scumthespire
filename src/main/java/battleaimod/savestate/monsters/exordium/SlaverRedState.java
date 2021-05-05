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
import com.megacrit.cardcrawl.monsters.exordium.SlaverRed;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SlaverRedState extends MonsterState {
    private final boolean usedEntangle;
    private final boolean firstTurn;

    public SlaverRedState(AbstractMonster monster) {
        super(monster);

        this.firstTurn = ReflectionHacks.getPrivate(monster, SlaverRed.class, "firstTurn");
        this.usedEntangle = ReflectionHacks.getPrivate(monster, SlaverRed.class, "usedEntangle");

        monsterTypeNumber = Monster.SLAVER_RED.ordinal();
    }

    public SlaverRedState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.firstTurn = parsed.get("first_turn").getAsBoolean();
        this.usedEntangle = parsed.get("used_entangle").getAsBoolean();


        monsterTypeNumber = Monster.SLAVER_RED.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SlaverRed result = new SlaverRed(offsetX, offsetY);
        populateSharedFields(result);

        ReflectionHacks.setPrivate(result, SlaverRed.class, "firstTurn", firstTurn);
        ReflectionHacks.setPrivate(result, SlaverRed.class, "usedEntangle", usedEntangle);

        return result;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("first_turn", firstTurn);
        monsterStateJson.addProperty("used_entangle", usedEntangle);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = SlaverRed.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 62)
        public static SpireReturn SlaverRed(SlaverRed _instance, float x, float y) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
