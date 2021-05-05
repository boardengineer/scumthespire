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
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BookOfStabbing;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class BookOfStabbingState extends MonsterState {
    private final int stabCount;

    public BookOfStabbingState(AbstractMonster monster) {
        super(monster);

        stabCount = ReflectionHacks
                .getPrivate(monster, BookOfStabbing.class, "stabCount");

        monsterTypeNumber = Monster.BOOK_OF_STABBING.ordinal();
    }

    public BookOfStabbingState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();
        this.stabCount = parsed.get("stab_count").getAsInt();

        monsterTypeNumber = Monster.BOOK_OF_STABBING.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        BookOfStabbing monster = new BookOfStabbing();

        ReflectionHacks
                .setPrivate(monster, BookOfStabbing.class, "stabCount", stabCount);

        populateSharedFields(monster);
        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("stab_count", stabCount);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = BookOfStabbing.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 39 )
        public static SpireReturn BookOfStabbing(BookOfStabbing _instance) {
            if (shouldGoFast()) {
                int stabDmg;
                int bigStabDmg;
                _instance.type = AbstractMonster.EnemyType.ELITE;
                _instance.dialogX = -70.0F * Settings.scale;
                _instance.dialogY = 50.0F * Settings.scale;
                if (AbstractDungeon.ascensionLevel >= 8) {
                    MonsterState.setHp(_instance, 168, 172);
                } else {
                    MonsterState.setHp(_instance, 160, 164);
                }
                if (AbstractDungeon.ascensionLevel >= 3) {
                    stabDmg = 7;
                    bigStabDmg = 24;
                } else {
                    stabDmg = 6;
                    bigStabDmg = 21;
                }
                _instance.damage.add(new DamageInfo(_instance, stabDmg));
                _instance.damage.add(new DamageInfo(_instance, bigStabDmg));

                ReflectionHacks
                        .setPrivate(_instance, BookOfStabbing.class, "stabDmg", stabDmg);
                ReflectionHacks
                        .setPrivate(_instance, BookOfStabbing.class, "bigStabDmg", bigStabDmg);

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = BookOfStabbing.class,
            paramtypez = {int.class},
            method = "getMove"
    )
    public static class SpyOnStabCountFix {
        public static void Postfix(BookOfStabbing _instance, int num) {
            if(!shouldGoFast())
            {
                int stabCount = ReflectionHacks
                        .getPrivate(_instance, BookOfStabbing.class, "stabCount");

                System.err.println("Going up to " + stabCount + " Stabs");
            }
        }
    }
}
