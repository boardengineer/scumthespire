package battleaimod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

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
}
