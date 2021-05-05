package battleaimod.savestate.monsters.beyond;

import basemod.ReflectionHacks;
import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.AwakenedOne;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class AwakenedOneState extends MonsterState {
    private final boolean form1;
    private final boolean firstTurn;

    public AwakenedOneState(AbstractMonster monster) {
        super(monster);

        this.form1 = ReflectionHacks.getPrivate(monster, AwakenedOne.class, "form1");
        this.firstTurn = ReflectionHacks.getPrivate(monster, AwakenedOne.class, "firstTurn");

        monsterTypeNumber = Monster.AWAKENED_ONE.ordinal();
    }

    public AwakenedOneState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.form1 = parsed.get("form1").getAsBoolean();
        this.firstTurn = parsed.get("first_turn").getAsBoolean();

        monsterTypeNumber = Monster.AWAKENED_ONE.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        AwakenedOne monster = new AwakenedOne(offsetX, offsetY);

        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, AwakenedOne.class, "form1", form1);
        ReflectionHacks.setPrivate(monster, AwakenedOne.class, "firstTurn", firstTurn);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("form1", form1);
        monsterStateJson.addProperty("first_turn", firstTurn);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = AwakenedOne.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {
        @SpireInsertPatch(loc = 93)
        public static SpireReturn Insert(AwakenedOne _instance, float x, float y) {
            if (shouldGoFast()) {
                if (AbstractDungeon.ascensionLevel >= 9) {
                    MonsterState.setHp(_instance, 320, 320);
                } else {
                    MonsterState.setHp(_instance, 300, 300);
                }
                _instance.damage.add(new DamageInfo(_instance, 20));
                _instance.damage.add(new DamageInfo(_instance, 6));
                _instance.damage.add(new DamageInfo(_instance, 40));
                _instance.damage.add(new DamageInfo(_instance, 18));
                _instance.damage.add(new DamageInfo(_instance, 10));

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AwakenedOne.class,
            paramtypez = {SpriteBatch.class},
            method = "render"
    )
    public static class NoRenderPatch {
        public static SpireReturn Prefix(AwakenedOne _instance, SpriteBatch sb) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AwakenedOne.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoUpdatePatch {
        public static SpireReturn Prefix(AwakenedOne _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
