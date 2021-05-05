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
import com.megacrit.cardcrawl.monsters.exordium.JawWorm;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class JawWormState extends MonsterState {
    private final boolean firstMove;

    public JawWormState(AbstractMonster monster) {
        super(monster);

        this.firstMove = ReflectionHacks.getPrivate(monster, JawWorm.class, "firstMove");

        monsterTypeNumber = Monster.JAWWORM.ordinal();
    }

    public JawWormState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.firstMove = parsed.get("first_move").getAsBoolean();

        monsterTypeNumber = Monster.JAWWORM.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        JawWorm result = new JawWorm(offsetX, offsetY);
        populateSharedFields(result);

        ReflectionHacks.setPrivate(result, JawWorm.class, "firstMove", firstMove);

        return result;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("first_move", firstMove);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = JawWorm.class,
            paramtypez = {float.class, float.class, boolean.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {

        @SpireInsertPatch(loc = 95)
        public static SpireReturn JawWorm(JawWorm _instance, float x, float y, boolean hard) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
