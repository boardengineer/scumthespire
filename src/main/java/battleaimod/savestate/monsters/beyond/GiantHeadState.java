package battleaimod.savestate.monsters.beyond;

import basemod.ReflectionHacks;
import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.GiantHead;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class GiantHeadState extends MonsterState {
    private final int count;

    public GiantHeadState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.GIANT_HEAD.ordinal();
        this.count = ReflectionHacks.getPrivate(monster, GiantHead.class, "count");
    }

    public GiantHeadState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.count = parsed.get("count").getAsInt();

        monsterTypeNumber = Monster.GIANT_HEAD.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        AbstractMonster monster = new GiantHead();

        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, GiantHead.class, "count", count);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("count", count);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = GiantHead.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {

        @SpireInsertPatch(loc = 42)
        public static SpireReturn Insert(GiantHead _instance) {
            if (shouldGoFast()) {
                _instance.type = AbstractMonster.EnemyType.ELITE;

                int startingDeathDmg;
                if (AbstractDungeon.ascensionLevel >= 8) {
                    MonsterState.setHp(_instance, 520, 520);
                } else {
                    MonsterState.setHp(_instance, 500, 500);
                }
                if (AbstractDungeon.ascensionLevel >= 3) {
                    startingDeathDmg = 40;
                } else {
                    startingDeathDmg = 30;
                }
                _instance.damage.add(new DamageInfo(_instance, 13));
                _instance.damage
                        .add(new DamageInfo(_instance, startingDeathDmg));
                _instance.damage
                        .add(new DamageInfo(_instance, startingDeathDmg + 5));
                _instance.damage
                        .add(new DamageInfo(_instance, startingDeathDmg + 10));
                _instance.damage
                        .add(new DamageInfo(_instance, startingDeathDmg + 15));
                _instance.damage
                        .add(new DamageInfo(_instance, startingDeathDmg + 20));
                _instance.damage
                        .add(new DamageInfo(_instance, startingDeathDmg + 25));
                _instance.damage
                        .add(new DamageInfo(_instance, startingDeathDmg + 30));

                ReflectionHacks
                        .setPrivate(_instance, GiantHead.class, "startingDeathDmg", startingDeathDmg);

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
