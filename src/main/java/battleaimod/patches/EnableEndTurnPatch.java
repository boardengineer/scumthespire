package battleaimod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.common.EnableEndTurnButtonAction;
import battleaimod.BattleAiMod;
import battleaimod.GameStateListener;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.vfx.combat.MoveNameEffect;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

@SpirePatch(
        clz = EnableEndTurnButtonAction.class,
        method = "update"
)
public class EnableEndTurnPatch {
    public static void Postfix(EnableEndTurnButtonAction _instance) {
        if (shouldGoFast()) {
            MoveNameEffect dd;
            CardCrawlGame game;
            BattleAiMod.readyForUpdate = true;
        }
        GameStateListener.signalTurnStart();
    }
}
