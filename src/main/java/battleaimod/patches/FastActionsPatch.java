package battleaimod.patches;

import basemod.ReflectionHacks;
import battleaimod.fastobjects.actions.DrawCardActionFast;
import battleaimod.fastobjects.actions.RollMoveActionFast;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class FastActionsPatch {
    @SpirePatch(
            clz = GameActionManager.class,
            paramtypez = {},
            method = "update"
    )
    public static class ForceGameActionsPatch {
        private static final AbstractGameAction lastAction = null;

        public static void Postfix(GameActionManager actionManager) {
            if (shouldGoFast()) {
                if (actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS || !actionManager.monsterQueue
                        .isEmpty()) {
                    while (actionManager.currentAction != null && !actionManager.currentAction.isDone) {
                        if (actionManager.currentAction instanceof RollMoveAction) {
                            AbstractMonster monster = ReflectionHacks
                                    .getPrivate(actionManager.currentAction, RollMoveAction.class, "monster");

                            actionManager.currentAction = new RollMoveActionFast(monster);
                        } else if (actionManager.currentAction instanceof DrawCardAction) {
                            actionManager.currentAction = new DrawCardActionFast(AbstractDungeon.player, actionManager.currentAction.amount);
                        }
                        System.out.println(actionManager.currentAction.getClass());
                        actionManager.currentAction.update();
                    }
                    actionManager.update();
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
}
