package communicationmod.patches;

import battleai.EndCommand;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.animations.AnimateHopAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.animations.SetAnimationAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.RestoreRetainedCardsAction;
import com.megacrit.cardcrawl.actions.utility.HandCheckAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import fastobjects.actions.DiscardCardActionFast;
import fastobjects.actions.DrawCardActionFast;
import fastobjects.actions.EmptyDeckShuffleActionFast;
import fastobjects.actions.RollMoveActionFast;

import java.util.HashSet;
import java.util.Set;


public class MonsterPatch {

    private static final Set<Class> FORCE_ONE_FRAME = new HashSet<Class>() {{
        add(DiscardCardActionFast.class);
        add(RestoreRetainedCardsAction.class);
        DrawCardAction d;
        add(DamageAction.class);
        add(ApplyPowerAction.class);
        add(UseCardAction.class);
        add(HandCheckAction.class);
        add(NewQueueCardAction.class);
        add(EndCommand.SomeAction.class);

        add(DrawCardActionFast.class);
        add(EmptyDeckShuffleActionFast.class);
        add(GainBlockAction.class);

        add(RollMoveActionFast.class);
        add(EnableEndTurnButtonAction.class);
    }};

    @SpirePatch(
            clz = AbstractMonster.class,
            paramtypez = {},
            method = "die"
    )
    public static class MonsterDeathPatch {
        public static void Postfix(AbstractMonster _instance) {
            Patcher patcher;
            _instance.useHopAnimation();
            System.err.println("dead worm?");
            _instance.deathTimer = .0000001F;
        }
    }

    @SpirePatch(
            clz = SetAnimationAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class SetAnimationPatch {
        public static void Replace(SetAnimationAction _instance) {
            _instance.isDone = true;
        }
    }

    @SpirePatch(
            clz = AnimateHopAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class HopAnimationPatch {
        public static void Replace(AnimateHopAction _instance) {
            _instance.isDone = true;
        }
    }

    @SpirePatch(
            clz = AnimateSlowAttackAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class SlowAttackAnimationPatch {
        public static void Replace(AnimateSlowAttackAction _instance) {
            _instance.isDone = true;
        }
    }

    @SpirePatch(
            clz = AnimateFastAttackAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FastAttackAnimationPatch {
        public static void Replace(AnimateFastAttackAction _instance) {
            _instance.isDone = true;
        }
    }

    @SpirePatch(
            clz = GameActionManager.class,
            paramtypez = {},
            method = "update"
    )
    public static class ForceGameActionsPatch {
        private static AbstractGameAction lastAction = null;

        public static void Postfix(GameActionManager actionManager) {
            if (actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS) {

                while (actionManager.currentAction != null && !actionManager.currentAction.isDone) {
                    actionManager.currentAction.update();
                }
                actionManager.update();


                if (actionManager.currentAction != null) {
                    if (lastAction == actionManager.currentAction) {
                        System.err.println(lastAction + " is taking multiple frames");
                    }
                    lastAction = actionManager.currentAction;
                }
            }
        }
    }
}
