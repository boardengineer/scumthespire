package battleaimod.savestate.monsters.beyond;

import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.OrbWalker;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class OrbWalkerState extends MonsterState {
    public OrbWalkerState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.ORG_WALKER.ordinal();
    }

    public OrbWalkerState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.ORG_WALKER.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        OrbWalker result = new OrbWalker(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = OrbWalker.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {
        @SpireInsertPatch(loc = 58)
        public static SpireReturn Insert(OrbWalker _instance, float x, float y) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
