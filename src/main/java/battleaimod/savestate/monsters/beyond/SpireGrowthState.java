package battleaimod.savestate.monsters.beyond;

import basemod.ReflectionHacks;
import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.SpireGrowth;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SpireGrowthState extends MonsterState {

    public SpireGrowthState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SPIRE_GROWTH.ordinal();
    }

    public SpireGrowthState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SPIRE_GROWTH.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SpireGrowth monster = new SpireGrowth();

        populateSharedFields(monster);

        return monster;
    }

    @SpirePatch(
            clz = SpireGrowth.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {
        @SpireInsertPatch(loc = 36)
        public static SpireReturn Insert(SpireGrowth _instance) {
            if (shouldGoFast()) {
                int tackleDmgActual;
                int smashDmgActual;
                if (AbstractDungeon.ascensionLevel >= 7) {
                    MonsterState.setHp(_instance, 190, 190);
                } else {
                    MonsterState.setHp(_instance, 170, 170);
                }
                if (AbstractDungeon.ascensionLevel >= 2) {
                    tackleDmgActual = 18;
                    smashDmgActual = 25;
                } else {
                    tackleDmgActual = 16;
                    smashDmgActual = 22;
                }

                _instance.damage.add(new DamageInfo(_instance, tackleDmgActual));
                _instance.damage.add(new DamageInfo(_instance, smashDmgActual));

                ReflectionHacks
                        .setPrivate(_instance, SpireGrowth.class, "tackleDmgActual", tackleDmgActual);
                ReflectionHacks
                        .setPrivate(_instance, SpireGrowth.class, "smashDmgActual", smashDmgActual);

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
