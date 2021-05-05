package battleaimod.savestate.monsters.city;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BanditPointy;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class BanditPointyState extends MonsterState {
    public BanditPointyState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.BANDIT_CHILD.ordinal();
    }

    public BanditPointyState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.BANDIT_CHILD.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        BanditPointy result = new BanditPointy(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = BanditPointy.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 54)
        public static SpireReturn BanditPointy(BanditPointy _instance, float x, float y) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
