package battleaimod.savestate.monsters.beyond;

import basemod.ReflectionHacks;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.Darkling;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class DarklinkState extends MonsterState {
    private final int chompDmg;
    private final int nipDmg;

    private final boolean firstMove;

    public DarklinkState(AbstractMonster monster) {
        super(monster);

        this.chompDmg = ReflectionHacks.getPrivate(monster, Darkling.class, "chompDmg");
        this.nipDmg = ReflectionHacks.getPrivate(monster, Darkling.class, "nipDmg");

        this.firstMove = ReflectionHacks.getPrivate(monster, Darkling.class, "firstMove");

        monsterTypeNumber = Monster.DARKLING.ordinal();
    }

    public DarklinkState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.chompDmg = parsed.get("chomp_dmg").getAsInt();
        this.nipDmg = parsed.get("nip_dmg").getAsInt();
        this.firstMove = parsed.get("fist_move").getAsBoolean();

        monsterTypeNumber = Monster.DARKLING.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Darkling monster = new Darkling(offsetX, offsetY);

        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, Darkling.class, "chompDmg", chompDmg);
        ReflectionHacks.setPrivate(monster, Darkling.class, "nipDmg", nipDmg);
        ReflectionHacks.setPrivate(monster, Darkling.class, "firstMove", firstMove);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("chomp_dmg", chompDmg);
        monsterStateJson.addProperty("nip_dmg", nipDmg);
        monsterStateJson.addProperty("fist_move", firstMove);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Darkling.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {
        @SpireInsertPatch(loc = 53)
        public static SpireReturn Insert(Darkling _instance, float x, float y) {
            if (shouldGoFast()) {
                int chompDmg;
                int nipDmg;

                if (AbstractDungeon.ascensionLevel >= 7) {
                    MonsterState.setHp(_instance, 50, 59);
                } else {
                    MonsterState.setHp(_instance, 48, 56);
                }
                if (AbstractDungeon.ascensionLevel >= 2) {
                    chompDmg = 9;
                    nipDmg = AbstractDungeon.monsterHpRng.random(9, 13);
                } else {
                    chompDmg = 8;
                    nipDmg = AbstractDungeon.monsterHpRng.random(7, 11);
                }

                _instance.damage.add(new DamageInfo(_instance, chompDmg));
                _instance.damage.add(new DamageInfo(_instance, nipDmg));

                ReflectionHacks.setPrivate(_instance, Darkling.class, "chompDmg", chompDmg);
                ReflectionHacks.setPrivate(_instance, Darkling.class, "nipDmg", nipDmg);

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
