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
import com.megacrit.cardcrawl.monsters.ending.SpireShield;

import static battleaimod.savestate.SaveStateMod.shouldGoFast;

public class SpireShieldState extends MonsterState {
    private final int moveCount;

    public SpireShieldState(AbstractMonster monster) {
        super(monster);

        this.moveCount = ReflectionHacks.getPrivate(monster, SpireShield.class, "moveCount");
    }

    public SpireShieldState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.moveCount = parsed.get("move_count").getAsInt();
    }

    @Override
    public AbstractMonster loadMonster() {
        SpireShield result = new SpireShield();

        populateSharedFields(result);

        ReflectionHacks.setPrivate(result, SpireShield.class, "moveCount", moveCount);

        return result;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("move_count", moveCount);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = SpireShield.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationConstructorPatch {
        @SpireInsertPatch(loc = 43)
        public static SpireReturn Insert(SpireShield spireShield) {
            if (shouldGoFast) {
                if (AbstractDungeon.ascensionLevel >= 8) {
                    MonsterState.setHp(spireShield, 125, 125);
                } else {
                    MonsterState.setHp(spireShield, 110, 110);
                }

                if (AbstractDungeon.ascensionLevel >= 3) {
                    spireShield.damage.add(new DamageInfo(spireShield, 14));
                    spireShield.damage.add(new DamageInfo(spireShield, 38));
                } else {
                    spireShield.damage.add(new DamageInfo(spireShield, 12));
                    spireShield.damage.add(new DamageInfo(spireShield, 34));
                }

                spireShield.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
