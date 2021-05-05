package battleaimod.savestate.monsters.exordium;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.AcidSlime_M;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class AcidSlime_MState extends MonsterState {
    public AcidSlime_MState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.ACID_SLIME_M.ordinal();
    }

    public AcidSlime_MState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.ACID_SLIME_M.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        AcidSlime_M result = new AcidSlime_M(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = AcidSlime_M.class,
            paramtypez = {float.class, float.class, int.class, int.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 65)
        public static SpireReturn AcidSlime_M(AcidSlime_M _instance, float x, float y, int poisonAmount, int newHealth) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
