package battleaimod.savestate.monsters.exordium;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.SpikeSlime_S;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SpikeSlime_SState extends MonsterState {
    public SpikeSlime_SState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SPIKE_SLIME_S.ordinal();
    }

    public SpikeSlime_SState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SPIKE_SLIME_S.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SpikeSlime_S result = new SpikeSlime_S(offsetX, offsetY, 0);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = SpikeSlime_S.class,
            paramtypez = {float.class, float.class, int.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 50)
        public static SpireReturn SpikeSlime_S(SpikeSlime_S _instance, float x, float y, int poisonAmount) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
