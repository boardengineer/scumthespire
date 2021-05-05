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
import com.megacrit.cardcrawl.monsters.beyond.Nemesis;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class NemesisState extends MonsterState {
    private final int scytheCooldown;
    private final boolean firstMove;

    public NemesisState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.NEMESIS.ordinal();

        this.scytheCooldown = ReflectionHacks.getPrivate(monster, Nemesis.class, "scytheCooldown");
        this.firstMove = ReflectionHacks.getPrivate(monster, Nemesis.class, "firstMove");
    }

    public NemesisState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.scytheCooldown = parsed.get("scythe_cooldown").getAsInt();
        this.firstMove = parsed.get("first_move").getAsBoolean();

        monsterTypeNumber = Monster.NEMESIS.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Nemesis monster = new Nemesis();

        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, Nemesis.class, "scytheCooldown", scytheCooldown);
        ReflectionHacks.setPrivate(monster, Nemesis.class, "firstMove", firstMove);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("scythe_cooldown", scytheCooldown);
        monsterStateJson.addProperty("first_move", firstMove);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Nemesis.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {

        @SpireInsertPatch(loc = 55)
        public static SpireReturn Insert(Nemesis _instance) {
            if (shouldGoFast()) {
                _instance.type = AbstractMonster.EnemyType.ELITE;
                int fireDmg;

                if (AbstractDungeon.ascensionLevel >= 8) {
                    MonsterState.setHp(_instance, 200, 200);
                } else {
                    MonsterState.setHp(_instance, 185, 185);
                }
                if (AbstractDungeon.ascensionLevel >= 3) {
                    fireDmg = 7;
                } else {
                    fireDmg = 6;
                }
                _instance.damage.add(new DamageInfo(_instance, 45));
                _instance.damage.add(new DamageInfo(_instance, fireDmg));

                ReflectionHacks.setPrivate(_instance, Nemesis.class, "fireDmg", fireDmg);

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = Nemesis.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoUpdatePatch {
        public static SpireReturn Prefix(Nemesis _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
