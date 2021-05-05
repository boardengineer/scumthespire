package battleaimod.savestate.monsters.beyond;

import basemod.ReflectionHacks;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.Spiker;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SpikerState extends MonsterState {
    private final int thornsCount;

    public SpikerState(AbstractMonster monster) {
        super(monster);

        this.thornsCount = ReflectionHacks.getPrivate(monster, Spiker.class, "thornsCount");
    }

    public SpikerState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.thornsCount = parsed.get("thorns_count").getAsInt();
    }

    @Override
    public AbstractMonster loadMonster() {
        Spiker monster = new Spiker(offsetX, offsetY);

        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, Spiker.class, "thornsCount", thornsCount);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("thorns_count", thornsCount);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Spiker.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {

        @SpireInsertPatch(loc = 48)
        public static SpireReturn Insert(Spiker _instance, float x, float y) {
            if (shouldGoFast()) {

                int startingThorns;
                int attackDmg;
                if (AbstractDungeon.ascensionLevel >= 7) {
                    MonsterState.setHp(_instance, 44, 60);
                } else {
                    MonsterState.setHp(_instance, 42, 56);
                }
                if (AbstractDungeon.ascensionLevel >= 2) {
                    startingThorns = 4;
                    attackDmg = 9;
                } else {
                    startingThorns = 3;
                    attackDmg = 7;
                }

                ReflectionHacks
                        .setPrivate(_instance, Spiker.class, "startingThorns", startingThorns);
                ReflectionHacks.setPrivate(_instance, Spiker.class, "attackDmg", attackDmg);

                _instance.damage.add(new DamageInfo(_instance, attackDmg));


                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
