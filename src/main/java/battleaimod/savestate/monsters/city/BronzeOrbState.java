package battleaimod.savestate.monsters.city;

import basemod.ReflectionHacks;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BronzeOrb;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class BronzeOrbState extends MonsterState {
    protected final int count;
    private final boolean usedStasis;

    public BronzeOrbState(AbstractMonster monster) {
        super(monster);

        count = ReflectionHacks
                .getPrivate(monster, BronzeOrb.class, "count");
        usedStasis = ReflectionHacks
                .getPrivate(monster, BronzeOrb.class, "usedStasis");

        monsterTypeNumber = Monster.BRONZE_ORB.ordinal();
    }

    public BronzeOrbState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.count = parsed.get("count").getAsInt();
        this.usedStasis = parsed.get("used_stasis").getAsBoolean();

        monsterTypeNumber = Monster.BRONZE_ORB.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        BronzeOrb monster = new BronzeOrb(offsetX, offsetY, count);
        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, BronzeOrb.class, "usedStasis", usedStasis);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("count", count);
        monsterStateJson.addProperty("used_stasis", usedStasis);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = BronzeOrb.class,
            paramtypez = {float.class, float.class, int.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoImgPatch {
        public static void Postfix(BronzeOrb _instance, float x, float y, int count) {
            if (shouldGoFast()) {
                Texture img = ReflectionHacks.getPrivate(_instance, AbstractMonster.class, "img");
                ReflectionHacks.setPrivate(_instance, AbstractMonster.class, "img", null);
                if (img != null) {
                    img.dispose();
                }
            }
        }
    }
}
