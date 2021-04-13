package battleaimod.patches;

import basemod.ReflectionHacks;
import battleaimod.BattleAiMod;
import battleaimod.battleai.BattleAiController;
import battleaimod.fastobjects.actions.DrawCardActionFast;
import battleaimod.fastobjects.actions.RollMoveActionFast;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;

import static battleaimod.patches.MonsterPatch.shouldGoFast;
import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;

public class FastActionsPatch {
    @SpirePatch(
            clz = AbstractRoom.class,
            paramtypez = {},
            method = "update"
    )
    public static class ForceGameActionsPatch {
        public static void Postfix(AbstractRoom room) {
            GameActionManager actionManager = AbstractDungeon.actionManager;
            if (shouldGoFast()) {

                if (actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS || !actionManager.monsterQueue
                        .isEmpty() || shouldStepAiController()) {
                    while (shouldWaitOnActions(actionManager) || shouldStepAiController()) {
                        if (shouldWaitOnActions(actionManager)) {
                            if (actionManager.currentAction instanceof RollMoveAction) {
                                AbstractMonster monster = ReflectionHacks
                                        .getPrivate(actionManager.currentAction, RollMoveAction.class, "monster");

                                actionManager.currentAction = new RollMoveActionFast(monster);
                            } else if (actionManager.currentAction instanceof DrawCardAction) {
                                actionManager.currentAction = new DrawCardActionFast(AbstractDungeon.player, actionManager.currentAction.amount);
                            }
                            if (actionManager.currentAction != null) {
                                actionManager.currentAction.update();
                            }
                        } else if (shouldStepAiController()) {
                            BattleAiMod.readyForUpdate = false;
                            BattleAiMod.battleAiController.step();
                        }

                        actionManager.update();
                        AbstractDungeon.player.update();
                    }
                }
            }
        }
    }

    @SpirePatch(
            clz = RemoveSpecificPowerAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FastRemovePowerPatch {
        public static void Prefix(RemoveSpecificPowerAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .1F);
            }
        }

        public static void Postfix(RemoveSpecificPowerAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = ApplyPowerAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FastApplyPowerActionPatch {
        public static void Prefix(ApplyPowerAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .1F);
            }
        }

        public static void Postfix(ApplyPowerAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
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
            clz = MakeTempCardInDiscardAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class MakeTempCardsFastPatch {
        public static void Prefix(MakeTempCardInDiscardAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .001F);

                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "startDuration", .001F);
            }
        }

        public static void Postfix(MakeTempCardInDiscardAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    private static boolean shouldStepAiController() {
        return BattleAiMod.battleAiController != null && !BattleAiMod.battleAiController.isDone && BattleAiMod.readyForUpdate && actionManager.phase == GameActionManager.Phase.WAITING_ON_USER;
    }

    private static boolean shouldWaitOnActions(GameActionManager actionManager) {
        return actionManager.currentAction != null && !actionManager.currentAction.isDone || !actionManager.monsterQueue
                .isEmpty() || !actionManager.actions.isEmpty();
    }


    @SpirePatch(
            clz = MonsterRoom.class,
            paramtypez = {},
            method = "onPlayerEntry"
    )
    public static class SpyOnMonsterRoomPatch {
        public static void Prefix(MonsterRoom _instance) {
            System.err.println("Starting fight " + AbstractDungeon.monsterList.get(0));
            BattleAiController.currentEncounter = AbstractDungeon.monsterList.get(0);
        }


    }

    @SpirePatch(
            clz = MonsterRoomElite.class,
            paramtypez = {},
            method = "onPlayerEntry"
    )
    public static class SpyOnEliteMonsterRoomPatch {
        public static void Prefix(MonsterRoomElite _instance) {
            System.err.println("Starting fight " + AbstractDungeon.eliteMonsterList.get(0));
            BattleAiController.currentEncounter = AbstractDungeon.eliteMonsterList.get(0);
        }
    }

    @SpirePatch(
            clz = MonsterRoomBoss.class,
            paramtypez = {},
            method = "onPlayerEntry"
    )
    public static class SpyOnBossMonsterRoomPatch {
        public static void Prefix(MonsterRoomBoss _instance) {
            System.err.println("Starting fight " + AbstractDungeon.bossList.get(0));
            BattleAiController.currentEncounter = AbstractDungeon.bossList.get(0);
        }
    }

//    @SpirePatch(
//            clz = TheGuardian.class,
//            paramtypez = {String.class},
//            method = "takeTurn"
//    )
//    public static class WhatGIsUpToPath {
//        public static void Replace(TheGuardian _instance, String stateName) {
//            System.err.println("G wants to " + _instance.nextMove);
//        }
//    }
}
