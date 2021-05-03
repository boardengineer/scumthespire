package battleaimod;

import basemod.ReflectionHacks;
import basemod.interfaces.PreUpdateSubscriber;
import battleaimod.battleai.BattleAiController;
import battleaimod.fastobjects.actions.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.IntentFlashAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.animations.SetAnimationAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.*;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToDiscardEffect;
import skrelpoid.superfastmode.SuperFastMode;

import java.util.Iterator;
import java.util.List;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SpeedController implements PreUpdateSubscriber {
    public static boolean goFast = false;

    @Override
    public void receivePreUpdate() {
        clearSomeActions(AbstractDungeon.actionManager.actions);
        clearSomeActions(AbstractDungeon.actionManager.preTurnActions);
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
            if (!AbstractDungeon.isScreenUp || AbstractDungeon.screen != AbstractDungeon.CurrentScreen.HAND_SELECT) {
                return;
            }
        } else {
            try {
//                AbstractDungeon.getCurrRoom().souls.
//                if (SoulGroup.isActive()) {
//                    return;
//                }
            } catch (Exception e) {
            }
        }

        if (!shouldGoFast()) {
            if (BattleAiController.shouldStep()) {
                BattleAiMod.readyForUpdate = false;
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

        SuperFastMode.deltaMultiplier = 100000000.0F;
        SuperFastMode.isInstantLerp = true;
        SuperFastMode.isDeltaMultiplied = true;
        Settings.DISABLE_EFFECTS = true;
        Iterator<AbstractGameEffect> topLevelEffects = AbstractDungeon.topLevelEffects.iterator();
        while (topLevelEffects.hasNext()) {
            AbstractGameEffect effect = topLevelEffects.next();
            effect.duration = Math.min(effect.duration, .005F);

            if (effect instanceof EnemyTurnEffect || effect instanceof PlayerTurnEffect) {
                effect.isDone = true;
            }

            if (effect instanceof CardTrailEffect) {
                effect.dispose();
                topLevelEffects.remove();
            } else if (effect instanceof FastCardObtainEffect) {
                // don't remove card obtain effects of they get skipped
            } else {
                effect.dispose();
                topLevelEffects.remove();
            }
        }

        Iterator<AbstractGameEffect> effectIterator = AbstractDungeon.effectList.iterator();
        while (effectIterator.hasNext()) {
            AbstractGameEffect effect = effectIterator.next();
            if (!(effect instanceof FastCardObtainEffect || effect instanceof ShowCardAndAddToDiscardEffect)) {
                effect.dispose();
                effectIterator.remove();
            }
        }

        clearActions(AbstractDungeon.actionManager.actions);
        clearActions(AbstractDungeon.actionManager.preTurnActions);
    }

    private static void clearActions(List<AbstractGameAction> actions) {
        for (int i = 0; i < actions.size(); i++) {
            AbstractGameAction action = actions.get(i);
            if (action instanceof WaitAction || action instanceof IntentFlashAction) {
                actions.remove(i);
                i--;
            } else if (action instanceof DrawCardAction) {
                actions.remove(i);
                actions.add(i, new DrawCardActionFast(AbstractDungeon.player, action.amount));
            } else if (action instanceof EmptyDeckShuffleAction) {
                actions.remove(i);
                actions.add(i, new EmptyDeckShuffleActionFast());
            } else if (action instanceof DiscardAction) {
                actions.remove(i);
                actions.add(i, new DiscardCardActionFast(action));
            } else if (action instanceof DiscardAtEndOfTurnAction) {
                actions.remove(i);
                actions.add(i, new DiscardAtEndOfTurnActionFast());
            } else if (action instanceof AnimateSlowAttackAction) {
                actions.remove(i);
                i--;
            } else if (action instanceof ShowMoveNameAction) {
                actions.remove(i);
                i--;
            } else if (action instanceof SetAnimationAction) {
                actions.remove(i);
                i--;
            } else if (action instanceof VFXAction) {
                actions.remove(i);
                i--;
            } else if (action instanceof GainBlockAction) {
//                actions.remove(i);
//                i--;
            } else if (action instanceof RollMoveAction) {
                AbstractMonster monster = ReflectionHacks
                        .getPrivate(action, RollMoveAction.class, "monster");
                actions.remove(i);
                actions.add(i, new RollMoveActionFast(monster));
            }
        }
    }

    private void clearSomeActions(List<AbstractGameAction> actions) {
        for (int i = 0; i < actions.size(); i++) {
            AbstractGameAction action = actions.get(i);
            if (action instanceof DrawCardAction) {
                actions.remove(i);
                actions.add(i, new DrawCardActionFast(AbstractDungeon.player, action.amount));
            }

            else if (action instanceof EmptyDeckShuffleAction) {
                actions.remove(i);
                actions.add(i, new EmptyDeckShuffleActionFast());
            }

            else if (action instanceof DiscardAction) {
                actions.remove(i);
                actions.add(i, new DiscardCardActionFast(action));
            } else if (action instanceof DiscardAtEndOfTurnAction) {
                actions.remove(i);
                actions.add(i, new DiscardAtEndOfTurnActionFast());
            }
//            else if (action instanceof RollMoveAction) {
//                AbstractMonster monster = ReflectionHacks
//                        .getPrivate(action, RollMoveAction.class, "monster");
//                actions.remove(i);
//                actions.add(i, new RollMoveActionFast(monster));
//            }
        }
    }
}
