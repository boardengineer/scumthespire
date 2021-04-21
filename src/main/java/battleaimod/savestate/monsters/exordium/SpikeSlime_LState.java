package battleaimod.savestate.monsters.exordium;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.SpikeSlime_L;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SpikeSlime_LState extends MonsterState {
    public SpikeSlime_LState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SPIKE_SLIME_L.ordinal();
    }

    public SpikeSlime_LState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SPIKE_SLIME_L.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SpikeSlime_L result = new SpikeSlime_L(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = SpikeSlime_L.class,
            paramtypez = {float.class, float.class, int.class, int.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 81)
        public static SpireReturn SpikeSlime_L(SpikeSlime_L _instance, float x, float y, int poisonAmount, int newHealth) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
