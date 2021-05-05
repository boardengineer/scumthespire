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
import com.megacrit.cardcrawl.monsters.beyond.Maw;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class MawState extends MonsterState {
    private final int turnCount;
    private final boolean roared;

    public MawState(AbstractMonster monster) {
        super(monster);

        this.turnCount = ReflectionHacks.getPrivate(monster, Maw.class, "turnCount");
        this.roared = ReflectionHacks.getPrivate(monster, Maw.class, "roared");

        monsterTypeNumber = Monster.MAW.ordinal();
    }

    public MawState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.turnCount = parsed.get("turn_count").getAsInt();
        this.roared = parsed.get("roared").getAsBoolean();

        monsterTypeNumber = Monster.MAW.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Maw monster = new Maw(offsetX, offsetY);

        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, Maw.class, "roared", roared);
        ReflectionHacks.setPrivate(monster, Maw.class, "turnCount", turnCount);

        monster.applyPowers();

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("turn_count", turnCount);
        monsterStateJson.addProperty("roared", roared);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Maw.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {

        @SpireInsertPatch(loc = 45)
        public static SpireReturn Insert(Maw _instance, float x, float y) {
            if (shouldGoFast()) {
                int strUp = 3;
                int terrifyDur = 3;
                int slamDmg;
                int nomDmg;
                if (AbstractDungeon.ascensionLevel >= 17) {
                    strUp += 2;
                    terrifyDur += 2;
                }
                if (AbstractDungeon.ascensionLevel >= 2) {
                    slamDmg = 30;
                    nomDmg = 5;
                } else {
                    slamDmg = 25;
                    nomDmg = 5;
                }
                _instance.damage.add(new DamageInfo(_instance, slamDmg));
                _instance.damage.add(new DamageInfo(_instance, nomDmg));

                ReflectionHacks.setPrivate(_instance, Maw.class, "strUp", strUp);
                ReflectionHacks.setPrivate(_instance, Maw.class, "terrifyDur", terrifyDur);
                ReflectionHacks.setPrivate(_instance, Maw.class, "slamDmg", slamDmg);
                ReflectionHacks.setPrivate(_instance, Maw.class, "nomDmg", nomDmg);

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
