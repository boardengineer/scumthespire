package battleaimod.savestate.monsters.exordium;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.SpikeSlime_M;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SpikeSlime_MState extends MonsterState {
    public SpikeSlime_MState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SPIKE_SLIME_M.ordinal();
    }

    public SpikeSlime_MState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SPIKE_SLIME_M.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SpikeSlime_M result = new SpikeSlime_M(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = SpikeSlime_M.class,
            paramtypez = {float.class, float.class, int.class, int.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 62)
        public static SpireReturn SpikeSlime_M(SpikeSlime_M _instance, float x, float y, int poisonAmount, int newHealth) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
