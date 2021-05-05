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
import com.megacrit.cardcrawl.monsters.city.ShelledParasite;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class ShelledParasiteState extends MonsterState {
    private final boolean firstMove;

    public ShelledParasiteState(AbstractMonster monster) {
        super(monster);

        firstMove = ReflectionHacks
                .getPrivate(monster, ShelledParasite.class, "firstMove");

        monsterTypeNumber = Monster.SHELLED_PARASITE.ordinal();
    }

    public ShelledParasiteState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.firstMove = parsed.get("first_move").getAsBoolean();

        monsterTypeNumber = Monster.SHELLED_PARASITE.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        ShelledParasite monster = new ShelledParasite(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, ShelledParasite.class, "firstMove", firstMove);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("first_move", firstMove);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = ShelledParasite.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 52)
        public static SpireReturn ShelledParasite(ShelledParasite _instance, float x, float y) {
            if (shouldGoFast()) {
                if (AbstractDungeon.ascensionLevel >= 7) {
                    MonsterState.setHp(_instance, 70, 75);
                } else {
                    MonsterState.setHp(_instance, 68, 72);
                }

                int doubleStrikeDmg;
                int fellDmg;
                int suckDmg;

                if (AbstractDungeon.ascensionLevel >= 2) {
                    doubleStrikeDmg = 7;
                    fellDmg = 21;
                    suckDmg = 12;
                } else {
                    doubleStrikeDmg = 6;
                    fellDmg = 18;
                    suckDmg = 10;
                }
                _instance.damage.add(new DamageInfo(_instance, doubleStrikeDmg));
                _instance.damage.add(new DamageInfo(_instance, fellDmg));
                _instance.damage.add(new DamageInfo(_instance, suckDmg));

                ReflectionHacks.setPrivate(_instance, ShelledParasite.class, "doubleStrikeDmg", doubleStrikeDmg);
                ReflectionHacks.setPrivate(_instance, ShelledParasite.class, "fellDmg", fellDmg);
                ReflectionHacks.setPrivate(_instance, ShelledParasite.class, "suckDmg", suckDmg);

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
