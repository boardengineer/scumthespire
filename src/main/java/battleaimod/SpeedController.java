package battleaimod;

import basemod.interfaces.PreUpdateSubscriber;
import battleaimod.simulator.ActionSimulator;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import skrelpoid.superfastmode.SuperFastMode;

import static battleaimod.simulator.patches.MonsterPatch.shouldGoFast;

public class SpeedController implements PreUpdateSubscriber {
    @Override
    public void receivePreUpdate() {
        if (shouldGoFast()) {
            makeGameVeryFast();
        } else {
            Settings.ACTION_DUR_XFAST = 0.1F;
            Settings.ACTION_DUR_FASTER = 0.2F;
            Settings.ACTION_DUR_FAST = 0.25F;
            Settings.ACTION_DUR_MED = 0.5F;
            Settings.ACTION_DUR_LONG = 1.0F;
            Settings.ACTION_DUR_XLONG = 1.5F;

            SuperFastMode.isDeltaMultiplied = false;
            Settings.DISABLE_EFFECTS = false;
            SuperFastMode.isInstantLerp = false;
        }

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

        if (!shouldGoFast()) {
            if (shouldStep()) {
                BattleAiMod.sendGameState();
            }
        }

    }

    private static void makeGameVeryFast() {
        Settings.ACTION_DUR_XFAST = 0.001F;
        Settings.ACTION_DUR_FASTER = 0.002F;
        Settings.ACTION_DUR_FAST = 0.0025F;
        Settings.ACTION_DUR_MED = 0.005F;
        Settings.ACTION_DUR_LONG = .01F;
        Settings.ACTION_DUR_XLONG = .015F;
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
