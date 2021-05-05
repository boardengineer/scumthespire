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
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.TimeEater;
import com.megacrit.cardcrawl.powers.TimeWarpPower;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class TimeEaterState extends MonsterState {
    private final boolean usedHaste;
    private final boolean firstTurn;

    public TimeEaterState(AbstractMonster monster) {
        super(monster);

        this.usedHaste = ReflectionHacks.getPrivate(monster, TimeEater.class, "usedHaste");
        this.firstTurn = ReflectionHacks.getPrivate(monster, TimeEater.class, "firstTurn");

        monsterTypeNumber = Monster.MAW.ordinal();
    }

    public TimeEaterState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.usedHaste = parsed.get("used_haste").getAsBoolean();
        this.firstTurn = parsed.get("fist_turn").getAsBoolean();

        monsterTypeNumber = Monster.MAW.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        TimeEater monster = new TimeEater();

        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, TimeEater.class, "usedHaste", usedHaste);
        ReflectionHacks.setPrivate(monster, TimeEater.class, "firstTurn", firstTurn);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("used_haste", usedHaste);
        monsterStateJson.addProperty("fist_turn", firstTurn);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = TimeEater.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {
        @SpireInsertPatch(loc = 65)
        public static SpireReturn Insert(TimeEater _instance) {
            if (shouldGoFast()) {
                int reverbDmg;
                int headSlamDmg;

                if (AbstractDungeon.ascensionLevel >= 9) {
                    MonsterState.setHp(_instance, 480, 480);
                } else {
                    MonsterState.setHp(_instance, 456, 456);
                }

                if (AbstractDungeon.ascensionLevel >= 4) {
                    reverbDmg = 8;
                    headSlamDmg = 32;
                } else {
                    reverbDmg = 7;
                    headSlamDmg = 26;
                }

                _instance.type = AbstractMonster.EnemyType.BOSS;
                _instance.damage
                        .add(new DamageInfo(_instance, reverbDmg, DamageInfo.DamageType.NORMAL));
                _instance.damage
                        .add(new DamageInfo(_instance, headSlamDmg, DamageInfo.DamageType.NORMAL));

                ReflectionHacks.setPrivate(_instance, TimeEater.class, "reverbDmg", reverbDmg);
                ReflectionHacks.setPrivate(_instance, TimeEater.class, "headSlamDmg", headSlamDmg);


                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = TimeWarpPower.class,
            paramtypez = {AbstractCard.class, UseCardAction.class},
            method = "onAfterUseCard"
    )
    public static class SpyOnPowerPatch {
        public static void Postfix(TimeWarpPower _instance, AbstractCard card, UseCardAction action) {
            if (shouldGoFast()) {
//                System.err.println(_instance.amount + " " + GameActionManager.turn);
            }
        }
    }
}
