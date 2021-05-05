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
import com.megacrit.cardcrawl.monsters.beyond.Deca;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class DecaState extends MonsterState {
    private final boolean isAttacking;

    public DecaState(AbstractMonster monster) {
        super(monster);

        this.isAttacking = ReflectionHacks.getPrivate(monster, Deca.class, "isAttacking");

        monsterTypeNumber = Monster.DECA.ordinal();
    }

    public DecaState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.isAttacking = parsed.get("is_attacking").getAsBoolean();

        monsterTypeNumber = Monster.DECA.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Deca monster = new Deca();

        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, Deca.class, "isAttacking", isAttacking);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("is_attacking", isAttacking);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Deca.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {
        @SpireInsertPatch(loc = 50)
        public static SpireReturn Insert(Deca _instance) {
            if (shouldGoFast()) {
                int beamDmg;

                _instance.type = AbstractMonster.EnemyType.BOSS;
                if (AbstractDungeon.ascensionLevel >= 9) {
                    MonsterState.setHp(_instance, 265, 265);
                } else {
                    MonsterState.setHp(_instance, 250, 250);
                }
                if (AbstractDungeon.ascensionLevel >= 4) {
                    beamDmg = 12;
                } else {
                    beamDmg = 10;
                }
                _instance.damage.add(new DamageInfo(_instance, beamDmg));

                ReflectionHacks
                        .setPrivate(_instance, Deca.class, "isAttacking", true);
                ReflectionHacks
                        .setPrivate(_instance, Deca.class, "beamDmg", beamDmg);

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
