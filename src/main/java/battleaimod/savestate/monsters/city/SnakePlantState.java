package battleaimod.savestate.monsters.city;

import basemod.ReflectionHacks;
import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.SnakePlant;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SnakePlantState extends MonsterState {
    public SnakePlantState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SNAKE_PLANT.ordinal();
    }

    public SnakePlantState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SNAKE_PLANT.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SnakePlant result = new SnakePlant(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = SnakePlant.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 43)
        public static SpireReturn SnakePlant(SnakePlant _instance) {
            if (shouldGoFast()) {
                int rainBlowsDmg;
                if (AbstractDungeon.ascensionLevel >= 7) {
                    MonsterState.setHp(_instance, 78, 82);
                } else {
                    MonsterState.setHp(_instance, 75, 79);
                }

                if (AbstractDungeon.ascensionLevel >= 2) {
                    rainBlowsDmg = 8;
                } else {
                    rainBlowsDmg = 7;
                }

                ReflectionHacks
                        .setPrivate(_instance, SnakePlant.class, "rainBlowsDmg", rainBlowsDmg);

                _instance.damage.add(new DamageInfo(_instance, rainBlowsDmg));
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
