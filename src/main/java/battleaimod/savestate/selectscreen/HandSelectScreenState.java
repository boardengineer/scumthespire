package battleaimod.savestate.selectscreen;

import basemod.ReflectionHacks;
import battleaimod.fastobjects.actions.DiscardCardActionFast;
import battleaimod.fastobjects.actions.DrawCardActionFast;
import battleaimod.fastobjects.actions.UpdateOnlyUseCardAction;
import battleaimod.savestate.CardState;
import battleaimod.savestate.PlayerState;
import battleaimod.savestate.SetMoveActionState;
import battleaimod.savestate.actions.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.ShoutAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.ArmamentsAction;
import com.megacrit.cardcrawl.actions.unique.DualWieldAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.TextAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class HandSelectScreenState {
    private final int numCardsToSelect;
    private final ArrayList<CardState> selectedCards;
    private final ArrayList<ActionState> actionQueue;
    private final CardState hoveredCard;
    private final boolean wereCardsRetrieved;
    private final boolean canPickZero;
    private final boolean upTo;
    private final boolean anyNumber;
    private final boolean forTransform;
    private final boolean forUpgrade;
    private final int numSelected;
    private final ActionState actionState;
    private final boolean isDisabled;
//    private final CardQueueItemState queueItemState;

    public HandSelectScreenState() {
        if (AbstractDungeon.handCardSelectScreen.hoveredCard != null) {
            this.hoveredCard = new CardState(AbstractDungeon.handCardSelectScreen.hoveredCard);
        } else {
            hoveredCard = null;
        }
        selectedCards = PlayerState
                .toCardStateArray(AbstractDungeon.handCardSelectScreen.selectedCards.group);

        this.numCardsToSelect = AbstractDungeon.handCardSelectScreen.numCardsToSelect;
        this.wereCardsRetrieved = AbstractDungeon.handCardSelectScreen.wereCardsRetrieved;
        this.canPickZero = AbstractDungeon.handCardSelectScreen.canPickZero;
        this.upTo = AbstractDungeon.handCardSelectScreen.upTo;
        this.anyNumber = ReflectionHacks
                .getPrivate(AbstractDungeon.handCardSelectScreen, HandCardSelectScreen.class, "anyNumber");
        this.forTransform = ReflectionHacks
                .getPrivate(AbstractDungeon.handCardSelectScreen, HandCardSelectScreen.class, "forTransform");
        this.forUpgrade = ReflectionHacks
                .getPrivate(AbstractDungeon.handCardSelectScreen, HandCardSelectScreen.class, "forUpgrade");
        this.numSelected = AbstractDungeon.handCardSelectScreen.numSelected;

        AbstractGameAction currentAction = AbstractDungeon.actionManager.currentAction;

        isDisabled = AbstractDungeon.handCardSelectScreen.button.isDisabled;

        if (currentAction != null) {
            if (currentAction instanceof ArmamentsAction) {
                actionState = new ArmamentsActionState(currentAction);
            } else if (currentAction instanceof DualWieldAction) {
                actionState = new DualWieldActionState(currentAction);
            } else if (currentAction instanceof ExhaustAction) {
                actionState = new ExhaustActionState(currentAction);
            } else if (currentAction instanceof DiscardAction) {
                actionState = new DiscardCardActionState((DiscardAction) currentAction);
            } else if (currentAction instanceof DiscardCardActionFast) {
                actionState = new DiscardCardActionState((DiscardCardActionFast) currentAction);
            } else {
                throw new IllegalStateException("this shouldn't happen " + AbstractDungeon.actionManager.currentAction);
            }

            actionQueue = new ArrayList<>();

            for (AbstractGameAction action : AbstractDungeon.actionManager.actions) {
                if (action instanceof UseCardAction) {
                    actionQueue.add(new UseCardActionState((UseCardAction) action));
                } else if (action instanceof UpdateOnlyUseCardAction) {
                    actionQueue.add(new UseCardActionState((UpdateOnlyUseCardAction) action));
                } else if (action instanceof DrawCardAction) {
                    actionQueue.add(new DrawCardActionState(action));
                } else if (action instanceof DrawCardActionFast) {
                    actionQueue.add(new DrawCardActionState(action));
                } else if (action instanceof RemoveSpecificPowerAction) {
                    actionQueue.add(new RemoveSpecificPowerActionState(action));
                } else if (action instanceof MakeTempCardInDrawPileAction) {
                    actionQueue.add(new MakeTempCardInDrawPileActionState(action));
                } else if (action instanceof GainBlockAction) {
                    actionQueue.add(new GainBlockActionState(action));
                } else if (action instanceof ChangeStateAction) {
                    actionQueue.add(new ChangeStateActionState(action));
                } else if (action instanceof LoseHPAction) {
                    actionQueue.add(new LoseHPActionState(action));
                } else if (action instanceof DamageAllEnemiesAction) {
                    actionQueue.add(new DamageAllEnemiesActionState(action));
                } else if (action instanceof SetMoveAction) {
                    actionQueue.add(new SetMoveActionState(action));
                } else if (action instanceof GainEnergyAction) {
                    actionQueue.add(new GainEnergyActionState(action));
                } else if (action instanceof ReducePowerAction) {
                    actionQueue.add(new ReducePowerActionState(action));
                } else if (action instanceof EscapeAction) {
                    actionQueue.add(new EscapeActionState(action));
                } else if (action instanceof ApplyPowerAction) {
                    actionQueue.add(new ApplyPowerActionState(action));
                } else if (action instanceof VFXAction) {
                    // Nothing
                } else if (action instanceof ShoutAction) {
                    // Nothing
                } else if (action instanceof TextAboveCreatureAction) {
                    // nothing
                } else if (action instanceof SFXAction) {
                    // visual only
                } else if (action instanceof RelicAboveCreatureAction) {
                    // Visual effect only, ignore
                } else {
                    throw new IllegalArgumentException("Illegal action type found in action manager: " + action);
                }
            }

            if (actionQueue.isEmpty()) {
                throw new IllegalStateException("this shouldn't happen " + AbstractDungeon.actionManager.actions);
            }
        } else {
            actionState = null;
            actionQueue = null;
        }

    }

    public void loadHandSelectScreenState() {
        AbstractDungeon.handCardSelectScreen.button.isDisabled = isDisabled;

        AbstractDungeon.handCardSelectScreen.selectedCards.group = this.selectedCards.stream()
                                                                                     .map(CardState::loadCard)
                                                                                     .collect(Collectors
                                                                                             .toCollection(ArrayList::new));

        AbstractDungeon.handCardSelectScreen.numSelected = numSelected;
        AbstractDungeon.handCardSelectScreen.numCardsToSelect = numCardsToSelect;
        AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = wereCardsRetrieved;
        AbstractDungeon.handCardSelectScreen.canPickZero = canPickZero;
        AbstractDungeon.handCardSelectScreen.upTo = upTo;

        ReflectionHacks
                .setPrivate(AbstractDungeon.handCardSelectScreen, HandCardSelectScreen.class, "anyNumber", anyNumber);
        ReflectionHacks
                .setPrivate(AbstractDungeon.handCardSelectScreen, HandCardSelectScreen.class, "forTransform", forTransform);
        ReflectionHacks
                .setPrivate(AbstractDungeon.handCardSelectScreen, HandCardSelectScreen.class, "forUpgrade", forUpgrade);

        AbstractDungeon.handCardSelectScreen.numSelected = numSelected;

        if (actionState != null) {
            AbstractDungeon.actionManager.currentAction = actionState.loadAction();
            AbstractDungeon.actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;

            actionQueue.forEach(action -> AbstractDungeon.actionManager.actions.add(action
                    .loadAction()));

            if (AbstractDungeon.actionManager.actions.isEmpty()) {
                throw new IllegalStateException("this too shouldn't happen");
            }

        }
    }
}
