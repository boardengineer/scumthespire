package battleaimod.savestate.monsters.exordium;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.AcidSlime_S;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class AcidSlime_SState extends MonsterState {
    public AcidSlime_SState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.ACID_SLIME_S.ordinal();
    }

    public AcidSlime_SState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.ACID_SLIME_S.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        AcidSlime_S result = new AcidSlime_S(offsetX, offsetY, 0);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = AcidSlime_S.class,
            paramtypez = {float.class, float.class, int.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 51)
        public static SpireReturn AcidSlime_S(AcidSlime_S _instance, float x, float y, int poisonAmount) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
