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
import com.megacrit.cardcrawl.monsters.city.Chosen;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class ChosenState extends MonsterState {
    private final boolean firstTurn;
    private final boolean usedHex;

    public ChosenState(AbstractMonster monster) {
        super(monster);

        firstTurn = ReflectionHacks
                .getPrivate(monster, Chosen.class, "firstTurn");
        usedHex = ReflectionHacks
                .getPrivate(monster, Chosen.class, "usedHex");

        monsterTypeNumber = Monster.CHOSEN.ordinal();
    }

    public ChosenState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.usedHex = parsed.get("used_hex").getAsBoolean();
        this.firstTurn = parsed.get("first_turn").getAsBoolean();

        monsterTypeNumber = Monster.CHOSEN.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Chosen monster = new Chosen(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, Chosen.class, "usedHex", usedHex);
        ReflectionHacks.setPrivate(monster, Chosen.class, "firstTurn", firstTurn);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("used_hex", usedHex);
        monsterStateJson.addProperty("first_turn", firstTurn);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Chosen.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 82)
        public static SpireReturn Chosen(Chosen _instance, float x, float y) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
