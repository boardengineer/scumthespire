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
import com.megacrit.cardcrawl.cards.red.Immolate;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.AcidSlime_L;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class AcidSlime_LState extends MonsterState {
    private final boolean splitTriggered;

    public AcidSlime_LState(AbstractMonster monster) {
        super(monster);

        this.splitTriggered = ReflectionHacks
                .getPrivate(monster, AcidSlime_L.class, "splitTriggered");

        monsterTypeNumber = Monster.ACID_SLIME_L.ordinal();
    }

    public AcidSlime_LState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        Immolate charge;

        this.splitTriggered = parsed.get("split_triggered").getAsBoolean();

        monsterTypeNumber = Monster.ACID_SLIME_L.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        AcidSlime_L result = new AcidSlime_L(offsetX, offsetY);
        populateSharedFields(result);

        ReflectionHacks.setPrivate(result, AcidSlime_L.class, "splitTriggered", splitTriggered);

        return result;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("split_triggered", splitTriggered);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = AcidSlime_L.class,
            paramtypez = {float.class, float.class, int.class, int.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 85)
        public static SpireReturn AcidSlime_L(AcidSlime_L _instance, float x, float y, int poisonAmount, int newHealth) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
