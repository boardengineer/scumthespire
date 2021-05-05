package battleaimod.savestate.monsters.city;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Taskmaster;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class TaskmasterState extends MonsterState {
    public TaskmasterState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SLAVER_BOSS.ordinal();
    }

    public TaskmasterState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SLAVER_BOSS.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Taskmaster result = new Taskmaster(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = Taskmaster.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 55)
        public static SpireReturn Taskmaster(Taskmaster _instance, float x, float y) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
