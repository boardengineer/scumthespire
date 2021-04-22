package battleaimod.savestate.monsters.exordium;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.SlimeBoss;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SlimeBossState extends MonsterState {
    public SlimeBossState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SLIME_BOSS.ordinal();
    }

    public SlimeBossState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SLIME_BOSS.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SlimeBoss result = new SlimeBoss();
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = SlimeBoss.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 90)
        public static SpireReturn SlimeBoss(SlimeBoss _instance) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
