package battleaimod.patches;

import basemod.ReflectionHacks;
import battleaimod.BattleAiMod;
import battleaimod.fastobjects.ActionSimulator;
import battleaimod.fastobjects.actions.DiscardCardActionFast;
import battleaimod.fastobjects.actions.DrawCardActionFast;
import battleaimod.fastobjects.actions.EmptyDeckShuffleActionFast;
import battleaimod.fastobjects.actions.RollMoveActionFast;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.SetAnimationAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.File;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;

import java.util.HashMap;

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
            long updateStartTime = System.currentTimeMillis();
            boolean firstIteration = true;
            GameActionManager actionManager = AbstractDungeon.actionManager;
            if (shouldGoFast()) {
                if (actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS || !actionManager.monsterQueue
                        .isEmpty() || shouldStepAiController()) {

                    while (shouldWaitOnActions() || shouldStepAiController()) {
                        long startTime = System.currentTimeMillis();

                        if (firstIteration) {
                            firstIteration = false;
                            System.err
                                    .println("first iteration loop " + actionManager.currentAction + " " + actionManager.phase + " " + AbstractDungeon.effectList
                                            .size() + " " + actionManager.actions.size()
                                            + " " + AbstractDungeon.topLevelEffects
                                            .size() + " " + AbstractDungeon.effectsQueue
                                            .size() + " " + actionManager.monsterQueue.size());
                        }

                        AbstractDungeon.topLevelEffects.clear();
                        AbstractDungeon.effectList.clear();
                        AbstractDungeon.effectsQueue.clear();
                      
                        // TODO this is going to have consequences
                        actionManager.cardsPlayedThisCombat.clear();

                        if (shouldWaitOnActions()) {
                            while (actionManager.currentAction != null && !AbstractDungeon.isScreenUp) {
                                if (actionManager.currentAction instanceof RollMoveAction) {
                                    AbstractMonster monster = ReflectionHacks
                                            .getPrivate(actionManager.currentAction, RollMoveAction.class, "monster");
                                    actionManager.currentAction = new RollMoveActionFast(monster);
                                } else if (actionManager.currentAction instanceof DrawCardAction) {
                                    actionManager.currentAction = new DrawCardActionFast(AbstractDungeon.player, actionManager.currentAction.amount);
                                } else if (actionManager.currentAction instanceof SetAnimationAction) {
                                    actionManager.currentAction = null;
                                } else if (actionManager.currentAction instanceof DiscardAction) {
                                    actionManager.currentAction = new DiscardCardActionFast(actionManager.currentAction);
                                } else if (actionManager.currentAction instanceof EmptyDeckShuffleAction) {
                                    actionManager.currentAction = new EmptyDeckShuffleActionFast();
                                } else if (actionManager.currentAction instanceof ShowMoveNameAction) {
                                    actionManager.currentAction = null;
                                } else if (actionManager.currentAction instanceof WaitAction) {
                                    actionManager.currentAction = null;
                                } else if (actionManager.currentAction instanceof SFXAction) {
                                    actionManager.currentAction = null;
                                }
                                if (actionManager.currentAction != null) {
                                    long actionStartTime = System.currentTimeMillis();
                                    Class actionClass = actionManager.currentAction.getClass();

                                    if (!actionManager.currentAction.isDone && !AbstractDungeon.isScreenUp) {
                                        actionManager.currentAction.update();
                                    }
                                    if (BattleAiMod.battleAiController != null && BattleAiMod.battleAiController.actionClassTimes != null) {
                                        long timeThisAction = (System
                                                .currentTimeMillis() - actionStartTime);
                                        BattleAiMod.battleAiController
                                                .addRuntime("Actions", timeThisAction);
                                        HashMap<Class, Long> actionClassTimes = BattleAiMod.battleAiController.actionClassTimes;
                                        if (actionClassTimes != null) {
                                            if (actionClassTimes.containsKey(actionClass)) {
                                                actionClassTimes.put(actionClass, actionClassTimes
                                                        .get(actionClass) + timeThisAction);
                                            } else {
                                                actionClassTimes.put(actionClass, timeThisAction);
                                            }
                                        }
                                    }
                                }

                                if (actionManager.currentAction != null && actionManager.currentAction.isDone && !AbstractDungeon.isScreenUp) {
                                    actionManager.currentAction = null;
                                }

                                runAndProfile("Action Manager Loop", () -> {
                                    if (!AbstractDungeon.isScreenUp) {
                                        actionManager.update();
                                    }
                                });
                            }
                        } else if (shouldStepAiController()) {
                            runAndProfile("Battle AI Step", () -> {
                                BattleAiMod.readyForUpdate = false;
                                BattleAiMod.battleAiController.step();
                            });
                        }

                        long startRoomUpdate = System.currentTimeMillis();

                        runAndProfile("Room Update", () -> {
                            if (!AbstractDungeon.isScreenUp) {
                                ActionSimulator.roomUpdate();
                            }
                        });

                        if (AbstractDungeon.player.currentHealth <= 0) {
                            if (actionManager.currentAction instanceof DamageAction) {
                                actionManager.update();
                            }
//                            break;
                        }

                        if (BattleAiMod.battleAiController != null) {
                            BattleAiMod.battleAiController.addRuntime("Update Loop Total", System
                                    .currentTimeMillis() - startTime);
                        }

                        if (actionManager.phase == GameActionManager.Phase.WAITING_ON_USER && actionManager.currentAction == null) {
                            BattleAiMod.readyForUpdate = true;
                        }
                    }

                    System.err
                            .println("exiting loop " + actionManager.currentAction + " " + actionManager.phase + " " + AbstractDungeon.effectList
                                    .size() + " " + actionManager.actions.size()
                                    + " " + AbstractDungeon.topLevelEffects
                                    .size() + " " + AbstractDungeon
                                    .getCurrRoom().waitTimer + " " + AbstractDungeon.effectsQueue
                                    .size() + " " + actionManager.monsterQueue.size());
                    if (actionManager.currentAction == null && !AbstractDungeon.isScreenUp) {
                        ActionSimulator.ActionManagerNextAction();
                        AbstractDungeon
                                .getCurrRoom().phase = AbstractRoom.RoomPhase.COMBAT;
                    }

                }
            }
        }
    }

    @SpirePatch(
            clz = AbstractRoom.class,
            paramtypez = {},
            method = "endBattle"
    )
    public static class EndBattlePatch {
        public static void Postfix(AbstractRoom _instance) {
            if (shouldGoFast()) {
                BattleAiMod.readyForUpdate = true;
            }
        }
    }

    @SpirePatch(
            clz = SaveFile.class,
            paramtypez = {SaveFile.SaveType.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoMakeSavePatch {
        public static SpireReturn Prefix(SaveFile _instance, SaveFile.SaveType type) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = SaveAndContinue.class,
            paramtypez = {SaveFile.class},
            method = "save"
    )
    public static class NoSavingPatch {
        public static SpireReturn Prefix(SaveFile save) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = File.class,
            paramtypez = {},
            method = "save"
    )
    public static class NoSavingOnOtherThreadPatch {
        public static SpireReturn Prefix(File _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = GainBlockAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class GainBlockActionFastPatch {
        public static void Prefix(GainBlockAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .001F);

                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "startDuration", .001F);
            }
        }

        public static void Postfix(GainBlockAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
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
                .isEmpty() || !actionManager.actions.isEmpty();
    }

    public static void runAndProfile(String name, Runnable runnable) {
        long start = System.currentTimeMillis();

        runnable.run();

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController.addRuntime(name, System.currentTimeMillis() - start);
        }
    }
}
