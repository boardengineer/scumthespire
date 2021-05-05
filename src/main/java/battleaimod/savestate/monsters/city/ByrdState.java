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
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.monsters.city.Byrd;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class ByrdState extends MonsterState {
    private final boolean firstMove;
    private final boolean isFlying;

    public ByrdState(AbstractMonster monster) {
        super(monster);

        firstMove = ReflectionHacks.getPrivate(monster, Byrd.class, "firstMove");
        isFlying = ReflectionHacks.getPrivate(monster, Byrd.class, "isFlying");

        monsterTypeNumber = Monster.BYRD.ordinal();
    }

    public ByrdState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        firstMove = parsed.get("first_move").getAsBoolean();
        isFlying = parsed.get("is_flying").getAsBoolean();

        monsterTypeNumber = Monster.BYRD.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Byrd monster = new Byrd(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, Byrd.class, "firstMove", firstMove);
        ReflectionHacks.setPrivate(monster, Byrd.class, "isFlying", isFlying);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("first_move", firstMove);
        monsterStateJson.addProperty("is_flying", isFlying);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Byrd.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 80)
        public static SpireReturn Byrd(Byrd _instance, float x, float y) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = Byrd.class,
            paramtypez = {String.class},
            method = "changeState"
    )
    public static class NoStateAnimationsPatch {
        public static SpireReturn Prefix(Byrd _instance, String stateName) {
            if (shouldGoFast()) {
                if (stateName.equals("GROUNDED")) {
                    _instance.setMove((byte) 4, AbstractMonster.Intent.STUN);
                    EnemyMoveInfo move = ReflectionHacks
                            .getPrivate(_instance, AbstractMonster.class, "move");
                    _instance.nextMove = move.nextMove;
                    ReflectionHacks.setPrivate(_instance, Byrd.class, "isFlying", false);
                }
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
