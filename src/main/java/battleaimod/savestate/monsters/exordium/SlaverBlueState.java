package battleaimod.savestate.monsters.exordium;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.SlaverBlue;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SlaverBlueState extends MonsterState {
    public SlaverBlueState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SLAVER_BLUE.ordinal();
    }

    public SlaverBlueState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SLAVER_BLUE.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SlaverBlue result = new SlaverBlue(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = SlaverBlue.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 56)
        public static SpireReturn SlaverBlue(SlaverBlue _instance, float x, float y) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
