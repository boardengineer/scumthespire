package battleaimod.patches;

import battleaimod.BattleAiMod;
import battleaimod.fastobjects.ActionSimulator;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.SetAnimationAction;
import com.megacrit.cardcrawl.actions.common.ShowMoveNameAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import static battleaimod.patches.MonsterPatch.shouldGoFast;
import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;

public class FastActionsPatch {
    @SpirePatch(
            clz = AbstractDungeon.class,
            paramtypez = {},
            method = "update"
    )
    public static class ForceGameActionsPatch {
        public static void Postfix(AbstractDungeon dungeon) {
            GameActionManager actionManager = AbstractDungeon.actionManager;
            if (shouldGoFast()) {
                if (actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS || !actionManager.monsterQueue
                        .isEmpty() || shouldStepAiController()) {

                    while (shouldWaitOnActions() || shouldStepAiController()) {
                        long startTime = System.currentTimeMillis();

                        AbstractDungeon.topLevelEffects.clear();
                        AbstractDungeon.effectList.clear();
                        AbstractDungeon.effectsQueue.clear();

                        // TODO this is going to have consequences
                        actionManager.cardsPlayedThisCombat.clear();

                        if (shouldWaitOnActions()) {
                            while (actionManager.currentAction != null && !AbstractDungeon.isScreenUp) {
                                if (actionManager.currentAction instanceof SetAnimationAction) {
                                    actionManager.currentAction = null;
                                } else if (actionManager.currentAction instanceof ShowMoveNameAction) {
                                    actionManager.currentAction = null;
                                } else if (actionManager.currentAction instanceof WaitAction) {
                                    actionManager.currentAction = null;
                                } else if (actionManager.currentAction instanceof SFXAction) {
                                    actionManager.currentAction = null;
                                }

                                if (actionManager.currentAction != null) {
                                    if (!actionManager.currentAction.isDone) {
                                        actionManager.currentAction.update();
                                    }
                                }

                                if (actionManager.currentAction != null &&
                                        actionManager.currentAction.isDone && !AbstractDungeon.isScreenUp) {
                                    actionManager.currentAction = null;
                                }

                                if (!AbstractDungeon.isScreenUp) {
                                    ActionSimulator.ActionManageUpdate();
                                }

                            }
                        } else if (shouldStepAiController()) {
                            BattleAiMod.battleAiController.step();
                        }

                        runAndProfile("Room Update", () -> {
                            if (!AbstractDungeon.isScreenUp) {
                                ActionSimulator.roomUpdate();
                            }
                        });

                        if (BattleAiMod.battleAiController != null) {
                            BattleAiMod.battleAiController.addRuntime("Update Loop Total", System
                                    .currentTimeMillis() - startTime);
                        }
                    }

                    System.err.println("exiting loop ");
                    if (actionManager.currentAction == null && !AbstractDungeon.isScreenUp) {
                        ActionSimulator.ActionManagerNextAction();
                        AbstractDungeon
                                .getCurrRoom().phase = AbstractRoom.RoomPhase.COMBAT;
                    }

                }
            }
        }
    }

    public static boolean shouldStepAiController() {
        if (BattleAiMod.battleAiController == null || BattleAiMod.battleAiController.isDone) {
            return false;
        }

        if (shouldWaitOnActions()) {
            return false;
        }

        if (AbstractDungeon.isScreenUp) {
            return true;
        }

        return actionManager.phase == GameActionManager.Phase.WAITING_ON_USER &&
                !BattleAiMod.battleAiController.runCommandMode;
    }

    private static boolean shouldWaitOnActions() {
        // Only freeze if the AI is pathing
        if (BattleAiMod.battleAiController == null || BattleAiMod.battleAiController.runCommandMode) {
            return false;
        }

        // Screens wait for users even though there are actions in the action manager
        if (AbstractDungeon.isScreenUp) {
            return false;
        }

        // Start of Turn
        if (actionManager.turnHasEnded && !AbstractDungeon.getMonsters()
                                                          .areMonstersBasicallyDead()) {
            return true;
        }

        // Middle of Monster turn
        if (!actionManager.monsterQueue.isEmpty()) {
            return true;
        }

        if (actionManager.usingCard) {
            return true;
        }

        return actionManager.currentAction != null || !actionManager.actions
                .isEmpty() || !actionManager.actions
                .isEmpty() || actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS;
    }

    public static void runAndProfile(String name, Runnable runnable) {
        long start = System.currentTimeMillis();

        runnable.run();

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController.addRuntime(name, System.currentTimeMillis() - start);
        }
    }
}
