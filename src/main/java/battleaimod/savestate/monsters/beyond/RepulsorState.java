package battleaimod.savestate.monsters.beyond;

import basemod.ReflectionHacks;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.Repulsor;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class RepulsorState extends MonsterState {
    public RepulsorState(AbstractMonster monster) {
        super(monster);
    }

    public RepulsorState(String jsonString) {
        super(jsonString);
    }

    @Override
    public AbstractMonster loadMonster() {
        Repulsor monster = new Repulsor(offsetX, offsetY);

        populateSharedFields(monster);

        return monster;
    }

    @SpirePatch(
            clz = Repulsor.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {

        @SpireInsertPatch(loc = 36)
        public static SpireReturn Insert(Repulsor _instance, float x, float y) {
            if (shouldGoFast()) {

                int dazeAmt = 2;
                int attackDmg;
                if (AbstractDungeon.ascensionLevel >= 7) {
                    MonsterState.setHp(_instance, 31, 38);
                } else {
                    MonsterState.setHp(_instance, 29, 35);
                }
                if (AbstractDungeon.ascensionLevel >= 2) {
                    attackDmg = 13;
                } else {
                    attackDmg = 11;
                }

                _instance.damage.add(new DamageInfo(_instance, attackDmg));

                ReflectionHacks.setPrivate(_instance, Repulsor.class, "dazeAmt", dazeAmt);
                ReflectionHacks.setPrivate(_instance, Repulsor.class, "attackDmg", attackDmg);


                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
