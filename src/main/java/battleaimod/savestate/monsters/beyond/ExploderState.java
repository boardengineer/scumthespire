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
import com.megacrit.cardcrawl.monsters.beyond.Exploder;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class ExploderState extends MonsterState {
    private final int turnCount;

    public ExploderState(AbstractMonster monster) {
        super(monster);

        this.turnCount = ReflectionHacks.getPrivate(monster, Exploder.class, "turnCount");
    }

    public ExploderState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.turnCount = parsed.get("turn_count").getAsInt();
    }

    @Override
    public AbstractMonster loadMonster() {
        Exploder monster = new Exploder(offsetX, offsetY);

        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, Exploder.class, "turnCount", turnCount);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("turn_count", turnCount);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Exploder.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {

        @SpireInsertPatch(loc = 45)
        public static SpireReturn Insert(Exploder _instance, float x, float y) {
            if (shouldGoFast()) {
                int attackDmg;
                if (AbstractDungeon.ascensionLevel >= 7) {
                    MonsterState.setHp(_instance, 30, 35);
                } else {
                    MonsterState.setHp(_instance, 30, 30);
                }
                if (AbstractDungeon.ascensionLevel >= 2) {
                    attackDmg = 11;
                } else {
                    attackDmg = 9;
                }
                _instance.damage.add(new DamageInfo(_instance, attackDmg));

                ReflectionHacks.setPrivate(_instance, Exploder.class, "attackDmg", attackDmg);

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
