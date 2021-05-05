package battleaimod.savestate.monsters.exordium;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinWarrior;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class GremlinWarriorState extends MonsterState {
    public GremlinWarriorState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.GREMLIN_WARRIOR.ordinal();
    }

    public GremlinWarriorState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.GREMLIN_WARRIOR.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinWarrior result = new GremlinWarrior(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = GremlinWarrior.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 51)
        public static SpireReturn GremlinWarrior(GremlinWarrior _instance, float x, float y) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
