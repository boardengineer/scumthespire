package battleaimod.savestate.monsters.city;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.TorchHead;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class TorchHeadState extends MonsterState {
    public TorchHeadState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.TORCH_HEAD.ordinal();
    }

    public TorchHeadState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.TORCH_HEAD.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        TorchHead result = new TorchHead(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = TorchHead.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 40)
        public static SpireReturn TorchHead(TorchHead _instance, float x, float y) {
            if (shouldGoFast()) {
                if (AbstractDungeon.ascensionLevel >= 9) {
                    MonsterState.setHp(_instance, 40, 45);
                } else {
                    MonsterState.setHp(_instance, 38, 40);
                }

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = TorchHead.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoUpdateAnimationsPatch {
        @SpireInsertPatch(loc = 71)
        public static SpireReturn TorchHead(TorchHead _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
