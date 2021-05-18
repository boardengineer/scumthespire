package battleaimod.simulator.patches;

import battleaimod.simulator.ActionSimulator;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import static battleaimod.simulator.patches.MonsterPatch.shouldGoFast;

public class FastActionsPatch {
    @SpirePatch(
            clz = AbstractDungeon.class,
            paramtypez = {},
            method = "update"
    )
    public static class ForceGameActionsPatch {
        public static void Postfix(AbstractDungeon dungeon) {
            if (shouldGoFast()) {
                ActionSimulator.actionLoop();
            }
        }
    }
}
