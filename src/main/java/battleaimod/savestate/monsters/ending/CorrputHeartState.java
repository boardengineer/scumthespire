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
import com.megacrit.cardcrawl.monsters.ending.CorruptHeart;

import static battleaimod.savestate.SaveStateMod.shouldGoFast;

public class CorrputHeartState extends MonsterState {
    private final int bloodHitCount;
    private final boolean isFirstMove;
    private final int moveCount;
    private final int buffCount;

    public CorrputHeartState(AbstractMonster monster) {
        super(monster);

        this.bloodHitCount = ReflectionHacks
                .getPrivate(monster, CorruptHeart.class, "bloodHitCount");
        this.isFirstMove = ReflectionHacks.getPrivate(monster, CorruptHeart.class, "isFirstMove");
        this.moveCount = ReflectionHacks.getPrivate(monster, CorruptHeart.class, "moveCount");
        this.buffCount = ReflectionHacks.getPrivate(monster, CorruptHeart.class, "buffCount");
    }

    public CorrputHeartState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.bloodHitCount = parsed.get("blood_hit_count").getAsInt();
        this.isFirstMove = parsed.get("is_first_move").getAsBoolean();
        this.moveCount = parsed.get("move_count").getAsInt();
        this.buffCount = parsed.get("buff_count").getAsInt();
    }

    @Override
    public AbstractMonster loadMonster() {
        CorruptHeart result = new CorruptHeart();

        populateSharedFields(result);

        ReflectionHacks.setPrivate(result, CorruptHeart.class, "bloodHitCount", bloodHitCount);
        ReflectionHacks.setPrivate(result, CorruptHeart.class, "isFirstMove", isFirstMove);
        ReflectionHacks.setPrivate(result, CorruptHeart.class, "moveCount", moveCount);
        ReflectionHacks.setPrivate(result, CorruptHeart.class, "buffCount", buffCount);

        return result;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("blood_hit_count", bloodHitCount);
        monsterStateJson.addProperty("is_first_move", isFirstMove);
        monsterStateJson.addProperty("move_count", moveCount);
        monsterStateJson.addProperty("buff_count", buffCount);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = CorruptHeart.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationConstructorPatch {
        @SpireInsertPatch(loc = 56)
        public static SpireReturn Insert(CorruptHeart corruptHeart) {
            if (shouldGoFast) {
                int bloodHitCount;

                if (AbstractDungeon.ascensionLevel >= 9) {
                    MonsterState.setHp(corruptHeart, 800, 800);
                } else {
                    MonsterState.setHp(corruptHeart, 750, 750);
                }

                if (AbstractDungeon.ascensionLevel >= 4) {
                    corruptHeart.damage.add(new DamageInfo(corruptHeart, 45));
                    corruptHeart.damage.add(new DamageInfo(corruptHeart, 2));
                    bloodHitCount = 15;

                } else {
                    corruptHeart.damage.add(new DamageInfo(corruptHeart, 40));
                    corruptHeart.damage.add(new DamageInfo(corruptHeart, 2));
                    bloodHitCount = 12;
                }

                ReflectionHacks
                        .setPrivate(corruptHeart, CorruptHeart.class, "bloodHitCount", bloodHitCount);

                corruptHeart.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
