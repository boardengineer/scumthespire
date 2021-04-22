package battleaimod.savestate.monsters.exordium;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.SlaverRed;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SlaverRedState extends MonsterState {
    public SlaverRedState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SLAVER_RED.ordinal();
    }

    public SlaverRedState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SLAVER_RED.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SlaverRed result = new SlaverRed(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = SlaverRed.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 62)
        public static SpireReturn SlaverRed(SlaverRed _instance, float x, float y) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
