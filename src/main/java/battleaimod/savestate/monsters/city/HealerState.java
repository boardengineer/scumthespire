package battleaimod.savestate.monsters.city;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Healer;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class HealerState extends MonsterState {
    public HealerState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.HEALER.ordinal();
    }

    public HealerState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.HEALER.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Healer result = new Healer(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = Healer.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 64)
        public static SpireReturn Healer(Healer _instance, float x, float y) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
