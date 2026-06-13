package battleaimod.networking;

import basemod.interfaces.PostUpdateSubscriber;
import battleaimod.battleai.evolution.EvolutionManager;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import battleaimod.BattleAiMod;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import static battleaimod.battleai.evolution.EvolutionManager.hasTriggeredThisTurn;

public class AutoPlayController implements PostUpdateSubscriber {




    private boolean inCombat() {
        return CardCrawlGame.isInARun() && AbstractDungeon.currMapNode != null && AbstractDungeon
                .getCurrRoom() != null && AbstractDungeon
                .getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT;
    }

    @Override
    public void receivePostUpdate() {

        if (!inCombat()) return;

        //if EvolutionManager not ready, don't do anything
        if(!EvolutionManager.canRunAutoBattler)return;

        // Trigger once per player turn when game is ready.
        if (!AbstractDungeon.actionManager.turnHasEnded
                && AbstractDungeon.actionManager.isEmpty()
                && !hasTriggeredThisTurn
                && BattleClientController.readyForUpdate()) {

            triggerSimulation();
            hasTriggeredThisTurn = true;
        }

        // Reset at turn end
        if (AbstractDungeon.actionManager.turnHasEnded) {
            hasTriggeredThisTurn = false;
        }
    }

    private void triggerSimulation() {
        if (!StatusAndControlThreads.canSendState()) {
            return;
        }
        try {
            if (BattleAiMod.aiClient == null) {
                BattleAiMod.aiClient = new AiClient();
            }

            BattleAiMod.aiClient.sendState();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
