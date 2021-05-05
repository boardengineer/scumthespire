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
import com.megacrit.cardcrawl.monsters.beyond.Transient;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class TransientState extends MonsterState {
    private final int count;

    public TransientState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.TRANSIENT.ordinal();

        this.count = ReflectionHacks.getPrivate(monster, Transient.class, "count");

    }

    public TransientState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.count = parsed.get("count").getAsInt();

        monsterTypeNumber = Monster.TRANSIENT.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Transient monster = new Transient();
        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, Transient.class, "count", count);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("count", count);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Transient.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {
        @SpireInsertPatch(loc = 38)
        public static SpireReturn Insert(Transient _instance) {
            if (shouldGoFast()) {
                _instance.gold = 1;
                int startingDeathDmg;

                if (AbstractDungeon.ascensionLevel >= 2) {
                    startingDeathDmg = 40;
                } else {
                    startingDeathDmg = 30;
                }

                _instance.damage
                        .add(new DamageInfo(_instance, startingDeathDmg));
                _instance.damage
                        .add(new DamageInfo(_instance, startingDeathDmg + 10));
                _instance.damage
                        .add(new DamageInfo(_instance, startingDeathDmg + 20));
                _instance.damage
                        .add(new DamageInfo(_instance, startingDeathDmg + 30));
                _instance.damage
                        .add(new DamageInfo(_instance, startingDeathDmg + 40));
                _instance.damage
                        .add(new DamageInfo(_instance, startingDeathDmg + 50));
                _instance.damage
                        .add(new DamageInfo(_instance, startingDeathDmg + 60));
                _instance.state = new AnimationStateFast();

                ReflectionHacks
                        .setPrivate(_instance, Transient.class, "startingDeathDmg", startingDeathDmg);

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
