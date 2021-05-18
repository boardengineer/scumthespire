package battleaimod.simulator;

import basemod.interfaces.PreUpdateSubscriber;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

public class LudicrousSpeedMod implements PreUpdateSubscriber {
    public static Controller controller = null;
    public static boolean plaidMode = false;

    @Override
    public void receivePreUpdate() {
        if (AbstractDungeon.actionManager == null || AbstractDungeon.player == null) {
            return;
        }

        if (AbstractDungeon.actionManager.turnHasEnded
                || (AbstractDungeon.actionManager.currentAction != null && AbstractDungeon.actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS)
                || !AbstractDungeon.actionManager.isEmpty()) {
            if (!(AbstractDungeon.isScreenUp
                    && (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.HAND_SELECT || AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID))) {
                return;
            }
        }

        if (!LudicrousSpeedMod.plaidMode) {
            if (shouldStep() && controller != null && !controller.isDone()) {
                controller.step();
            }
        }
    }

    public static boolean shouldStep() {
        return shouldCheckForPlays() || isEndCommandAvailable() || ActionSimulator
                .shouldStepAiController();
    }

    public static boolean isInDungeon() {
        return CardCrawlGame.mode == CardCrawlGame.GameMode.GAMEPLAY && AbstractDungeon
                .isPlayerInDungeon() && AbstractDungeon.currMapNode != null;
    }

    private static boolean shouldCheckForPlays() {
        return isInDungeon() && (AbstractDungeon
                .getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && !AbstractDungeon.isScreenUp);
    }

    private static boolean isEndCommandAvailable() {
        return isInDungeon() && AbstractDungeon
                .getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && !AbstractDungeon.isScreenUp;
    }
}
