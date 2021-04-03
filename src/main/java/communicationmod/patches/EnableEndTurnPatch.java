package communicationmod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.common.EnableEndTurnButtonAction;
import communicationmod.CommunicationMod;
import communicationmod.GameStateListener;

import static communicationmod.patches.MonsterPatch.shouldGoFast;

@SpirePatch(
        clz = EnableEndTurnButtonAction.class,
        method = "update"
)
public class EnableEndTurnPatch {
    public static void Postfix(EnableEndTurnButtonAction _instance) {
        if (shouldGoFast()) {
            CommunicationMod.readyForUpdate = true;
        }
        GameStateListener.signalTurnStart();
    }
}
