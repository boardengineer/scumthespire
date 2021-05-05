package battleaimod.savestate.monsters.exordium;

import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.FungiBeast;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class FungiBeastState extends MonsterState {
    public FungiBeastState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.FUNGI_BEAST.ordinal();
    }

    public FungiBeastState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.FUNGI_BEAST.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        FungiBeast result = new FungiBeast(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = FungiBeast.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {

        @SpireInsertPatch(loc = 57)
        public static SpireReturn FungiBeast(FungiBeast _instance, float x, float y) {
            int biteDamage;
            if (AbstractDungeon.ascensionLevel >= 2) {
                biteDamage = 6;
            } else {
                biteDamage = 6;
            }

            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                _instance.damage.add(new DamageInfo(_instance, biteDamage));

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
