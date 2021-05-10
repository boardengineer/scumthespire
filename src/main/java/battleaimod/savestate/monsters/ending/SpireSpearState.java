package battleaimod.savestate.monsters.ending;

import basemod.ReflectionHacks;
import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.ending.SpireSpear;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SpireSpearState extends MonsterState {
    private final int moveCount;
    private final int skewerCount;

    public SpireSpearState(AbstractMonster monster) {
        super(monster);

        this.moveCount = ReflectionHacks.getPrivate(monster, SpireSpear.class, "moveCount");
        this.skewerCount = ReflectionHacks.getPrivate(monster, SpireSpear.class, "skewerCount");
    }

    public SpireSpearState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.moveCount = parsed.get("move_count").getAsInt();
        this.skewerCount = parsed.get("skewer_count").getAsInt();
    }

    @Override
    public AbstractMonster loadMonster() {
        SpireSpear result = new SpireSpear();

        populateSharedFields(result);

        ReflectionHacks.setPrivate(result, SpireSpear.class, "moveCount", moveCount);
        ReflectionHacks.setPrivate(result, SpireSpear.class, "skewerCount", skewerCount);

        return result;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("move_count", moveCount);
        monsterStateJson.addProperty("skewer_count", skewerCount);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = SpireSpear.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationConstructorPatch {
        @SpireInsertPatch(loc = 42)
        public static SpireReturn Insert(SpireSpear spireSpear) {
            if (shouldGoFast()) {
                int skewerCount;

                if (AbstractDungeon.ascensionLevel >= 8) {
                    MonsterState.setHp(spireSpear, 180, 180);
                } else {
                    MonsterState.setHp(spireSpear, 160, 160);
                }

                if (AbstractDungeon.ascensionLevel >= 3) {
                    skewerCount = 4;
                    spireSpear.damage.add(new DamageInfo(spireSpear, 6));
                    spireSpear.damage.add(new DamageInfo(spireSpear, 10));
                } else {
                    skewerCount = 3;
                    spireSpear.damage.add(new DamageInfo(spireSpear, 5));
                    spireSpear.damage.add(new DamageInfo(spireSpear, 10));
                }

                ReflectionHacks
                        .setPrivate(spireSpear, SpireSpear.class, "skewerCount", skewerCount);

                spireSpear.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
