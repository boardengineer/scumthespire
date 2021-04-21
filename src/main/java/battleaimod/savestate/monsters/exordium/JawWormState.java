package battleaimod.savestate.monsters.exordium;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.JawWorm;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class JawWormState extends MonsterState {
    public JawWormState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.JAWWORM.ordinal();
    }

    public JawWormState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.JAWWORM.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        JawWorm result = new JawWorm(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = JawWorm.class,
            paramtypez = {float.class, float.class, boolean.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {

        @SpireInsertPatch(loc = 95)
        public static SpireReturn JawWorm(JawWorm _instance, float x, float y, boolean hard) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
