package battleaimod.savestate.monsters.city;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BanditBear;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class BanditBearState extends MonsterState {
    public BanditBearState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.BANDIT_BEAR.ordinal();
    }

    public BanditBearState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.BANDIT_BEAR.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        BanditBear result = new BanditBear(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = BanditBear.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 65)
        public static SpireReturn BanditBear(BanditBear _instance, float x, float y) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
