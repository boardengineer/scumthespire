package battleaimod.savestate.monsters.exordium;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinNob;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class GremlinNobState extends MonsterState {
    public GremlinNobState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.GREMLIN_NOB.ordinal();
    }

    public GremlinNobState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.GREMLIN_NOB.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinNob result = new GremlinNob(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = GremlinNob.class,
            paramtypez = {float.class, float.class, boolean.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 70)
        public static SpireReturn GremlinNob(GremlinNob _instance, float x, float y, boolean setVuln) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
