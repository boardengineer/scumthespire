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
import com.megacrit.cardcrawl.monsters.exordium.SlimeBoss;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SlimeBossState extends MonsterState {
    private final boolean firstTurn;

    public SlimeBossState(AbstractMonster monster) {
        super(monster);

        this.firstTurn = ReflectionHacks.getPrivate(monster, SlimeBoss.class, "firstTurn");

        monsterTypeNumber = Monster.SLIME_BOSS.ordinal();
    }

    public SlimeBossState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.firstTurn = parsed.get("first_turn").getAsBoolean();

        monsterTypeNumber = Monster.SLIME_BOSS.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SlimeBoss result = new SlimeBoss();
        populateSharedFields(result);

        ReflectionHacks.setPrivate(result, SlimeBoss.class, "firstTurn", firstTurn);

        return result;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("first_turn", firstTurn);

        return monsterStateJson.toString();
    }


    @SpirePatch(
            clz = SlimeBoss.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 90)
        public static SpireReturn SlimeBoss(SlimeBoss _instance) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
