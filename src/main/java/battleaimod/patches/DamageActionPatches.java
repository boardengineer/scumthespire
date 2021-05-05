package battleaimod.patches;

import basemod.ReflectionHacks;
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
        public static SpireReturn Prefix(DamageAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;

                if (_instance.target == null || _instance.source != null && _instance.source.isDying || _instance.target
                        .isDeadOrEscaped()) {
                    SpireReturn.Return(null);
                }

                DamageInfo info = ReflectionHacks.getPrivate(_instance, DamageAction.class, "info");

                if (info.type != DamageInfo.DamageType.THORNS && (info.owner.isDying || info.owner.halfDead)) {
                    return SpireReturn.Return(null);
                }


                int goldAmount = ReflectionHacks
                        .getPrivate(_instance, DamageAction.class, "goldAmount");

                if (goldAmount != 0) {
                    ReflectionHacks.privateMethod(DamageAction.class, "stealGold")
                                   .invoke(_instance);
                }

                _instance.target.damage(info);
                if (AbstractDungeon.getCurrRoom().monsters.areMonstersBasicallyDead()) {
                    AbstractDungeon.actionManager.clearPostCombatActions();
                }

                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
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

                for (AbstractPower power : AbstractDungeon.player.powers) {
                    power.onDamageAllEnemies(_instance.damage);
                }

                List<AbstractMonster> monsters = AbstractDungeon.getCurrRoom().monsters.monsters;

                for (int i = 0; i < monsters.size(); i++) {
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
