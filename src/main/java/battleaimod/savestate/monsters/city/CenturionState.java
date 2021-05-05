package battleaimod.savestate.monsters.city;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Centurion;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class CenturionState extends MonsterState {
    public CenturionState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.CENTURION.ordinal();
    }

    public CenturionState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.CENTURION.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Centurion result = new Centurion(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = Centurion.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 62)
        public static SpireReturn Centurion(Centurion _instance, float x, float y) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
