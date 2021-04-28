package battleaimod.patches;

import battleaimod.BattleAiMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

import java.util.List;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class DamageActionPatches {
    @SpirePatch(
            clz = DamageAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class SpyOnDamageUpdatePatch {
        public static void Prefix(DamageAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }

        public static void Postfix(DamageAction _instance) {
            // TODO: I'm forcing this to only trigger once, why is this necessary
            _instance.source = null;
            new DamageInfo(AbstractDungeon.player, 0, DamageInfo.DamageType.NORMAL);
        }
    }


    @SpirePatch(
            clz = DamageAllEnemiesAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class SpyOnDamageAllEnemiesUpdatePatch {
        public static SpireReturn Prefix(DamageAllEnemiesAction _instance) {
            if (shouldGoFast()) {
                long startUpdate = System.currentTimeMillis();

                for(AbstractPower power: AbstractDungeon.player.powers) {
                    power.onDamageAllEnemies(_instance.damage);
                }

                List<AbstractMonster> monsters = AbstractDungeon.getCurrRoom().monsters.monsters;

                for(int i = 0; i < monsters.size(); i++) {
                    AbstractMonster monster = monsters.get(i);

                    monster.damage(new DamageInfo(_instance.source, _instance.damage[i], _instance.damageType));
                }


                if (AbstractDungeon.getCurrRoom().monsters.areMonstersBasicallyDead()) {
                    AbstractDungeon.actionManager.clearPostCombatActions();
                }

                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController.addRuntime("All Damage Update", System
                            .currentTimeMillis() - startUpdate);
                }

                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
