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
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Champ;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class ChampState extends MonsterState {
    private final int numTurns;
    private final int forgeTimes;
    private final int forgeThreshold;
    private final boolean thresholdReached;
    private final boolean firstTurn;

    public ChampState(AbstractMonster monster) {
        super(monster);

        numTurns = ReflectionHacks
                .getPrivate(monster, Champ.class, "numTurns");
        forgeTimes = ReflectionHacks
                .getPrivate(monster, Champ.class, "forgeTimes");
        forgeThreshold = ReflectionHacks
                .getPrivate(monster, Champ.class, "forgeThreshold");
        thresholdReached = ReflectionHacks
                .getPrivate(monster, Champ.class, "thresholdReached");
        firstTurn = ReflectionHacks
                .getPrivate(monster, Champ.class, "firstTurn");

        monsterTypeNumber = Monster.CHAMP.ordinal();
    }

    public ChampState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.numTurns = parsed.get("num_turns").getAsInt();
        this.forgeTimes = parsed.get("forge_times").getAsInt();
        this.forgeThreshold = parsed.get("forge_threshold").getAsInt();
        this.thresholdReached = parsed.get("threshold_reached").getAsBoolean();
        this.firstTurn = parsed.get("first_turn").getAsBoolean();

        monsterTypeNumber = Monster.CHAMP.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Champ monster = new Champ();
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, Champ.class, "numTurns", numTurns);
        ReflectionHacks
                .setPrivate(monster, Champ.class, "forgeTimes", forgeTimes);
        ReflectionHacks
                .setPrivate(monster, Champ.class, "forgeThreshold", forgeThreshold);
        ReflectionHacks
                .setPrivate(monster, Champ.class, "thresholdReached", thresholdReached);
        ReflectionHacks
                .setPrivate(monster, Champ.class, "firstTurn", firstTurn);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("num_turns", numTurns);
        monsterStateJson.addProperty("forge_times", forgeTimes);
        monsterStateJson.addProperty("forge_threshold", forgeThreshold);
        monsterStateJson.addProperty("threshold_reached", thresholdReached);
        monsterStateJson.addProperty("first_turn", firstTurn);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Champ.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 73)
        public static SpireReturn Champ(Champ _instance) {
            if (shouldGoFast()) {
                int slashDmg;
                int executeDmg;
                int slapDmg;
                int forgeAmt;
                int blockAmt;
                int strAmt;

                if (AbstractDungeon.ascensionLevel >= 9) {
                    MonsterState.setHp(_instance, 440, 440);
                } else {
                    MonsterState.setHp(_instance, 420, 420);
                }
                if (AbstractDungeon.ascensionLevel >= 19) {
                    slashDmg = 18;
                    executeDmg = 10;
                    slapDmg = 14;
                    strAmt = 4;
                    forgeAmt = 7;
                    blockAmt = 20;
                } else if (AbstractDungeon.ascensionLevel >= 9) {
                    slashDmg = 18;
                    executeDmg = 10;
                    slapDmg = 14;
                    strAmt = 3;
                    forgeAmt = 6;
                    blockAmt = 18;
                } else if (AbstractDungeon.ascensionLevel >= 4) {
                    slashDmg = 18;
                    executeDmg = 10;
                    slapDmg = 14;
                    strAmt = 3;
                    forgeAmt = 5;
                    blockAmt = 15;
                } else {
                    slashDmg = 16;
                    executeDmg = 10;
                    slapDmg = 12;
                    strAmt = 2;
                    forgeAmt = 5;
                    blockAmt = 15;
                }

                _instance.damage.add(new DamageInfo(_instance, slashDmg));
                _instance.damage.add(new DamageInfo(_instance, executeDmg));
                _instance.damage.add(new DamageInfo(_instance, slapDmg));

                ReflectionHacks
                        .setPrivate(_instance, Champ.class, "slashDmg", slashDmg);

                ReflectionHacks
                        .setPrivate(_instance, Champ.class, "executeDmg", executeDmg);

                ReflectionHacks
                        .setPrivate(_instance, Champ.class, "slapDmg", slapDmg);

                ReflectionHacks
                        .setPrivate(_instance, Champ.class, "forgeAmt", forgeAmt);

                ReflectionHacks
                        .setPrivate(_instance, Champ.class, "blockAmt", blockAmt);

                ReflectionHacks
                        .setPrivate(_instance, Champ.class, "strAmt", strAmt);

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
