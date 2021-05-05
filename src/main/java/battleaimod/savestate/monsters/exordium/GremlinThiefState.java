package battleaimod.savestate.monsters.exordium;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinThief;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class GremlinThiefState extends MonsterState {
    public GremlinThiefState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.GREMLIN_THIEF.ordinal();
    }

    public GremlinThiefState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.GREMLIN_THIEF.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinThief result = new GremlinThief(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = GremlinThief.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 51)
        public static SpireReturn GremlinThief(GremlinThief _instance, float x, float y) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
