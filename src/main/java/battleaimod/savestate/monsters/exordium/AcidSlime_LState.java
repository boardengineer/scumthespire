package battleaimod.savestate.monsters.exordium;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.AcidSlime_L;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class AcidSlime_LState extends MonsterState {
    public AcidSlime_LState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.ACID_SLIME_L.ordinal();
    }

    public AcidSlime_LState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.ACID_SLIME_L.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        AcidSlime_L result = new AcidSlime_L(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = AcidSlime_L.class,
            paramtypez = {float.class, float.class, int.class, int.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 85)
        public static SpireReturn AcidSlime_L(AcidSlime_L _instance, float x, float y, int poisonAmount, int newHealth) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
