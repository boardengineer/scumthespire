package battleaimod.savestate;

import basemod.ReflectionHacks;
import battleaimod.fastobjects.actions.DiscardCardActionFast;
import battleaimod.fastobjects.actions.DrawCardActionFast;
import battleaimod.fastobjects.actions.UpdateOnlyUseCardAction;
import battleaimod.savestate.actions.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.defect.SeekAction;
import com.megacrit.cardcrawl.actions.unique.ArmamentsAction;
import com.megacrit.cardcrawl.actions.unique.AttackFromDeckToHandAction;
import com.megacrit.cardcrawl.actions.unique.DiscardPileToTopOfDeckAction;
import com.megacrit.cardcrawl.actions.unique.DualWieldAction;
import com.megacrit.cardcrawl.actions.unique.ExhumeAction;
import com.megacrit.cardcrawl.actions.unique.SkillFromDeckToHandAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
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
                actionState = new DiscardActionState((DiscardAction) currentAction);
            } else if (currentAction instanceof DiscardCardActionFast) {
                actionState = new DiscardActionState((DiscardCardActionFast) currentAction);
            } else if (currentAction instanceof ExhumeAction) {     // Grid actions
                actionState = null;
            } else if (currentAction instanceof DiscardPileToTopOfDeckAction) {
                actionState = null;
            } else if (currentAction instanceof SeekAction) {
                actionState = null;
            } else if (currentAction instanceof SkillFromDeckToHandAction) {
                actionState = null;
            } else if (currentAction instanceof AttackFromDeckToHandAction) {
                actionState = null;
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
                    // Duplication + burning blood triggers this
                    actionQueue.add(new RemoveSpecificPowerActionState(action));
                } else if (action instanceof MakeTempCardInDrawPileAction) {
                    actionQueue.add(new MakeTempCardInDrawPileActionState(action));
                } else if (action instanceof RelicAboveCreatureAction) {
                    // Visual effect only, ignore
                } else if (action instanceof DamageAllEnemiesAction) {
                    actionQueue.add(new DamageAllEnemiesActionState((DamageAllEnemiesAction) action));
                } else if (action instanceof SFXAction || action instanceof VFXAction) {
                    // Ignore
                } else {
                    throw new IllegalArgumentException("Illegal action type found in action manager: " + action);
                }
            }

//            useCardActions = AbstractDungeon.actionManager.actions.stream()
//                                                                  .filter(action -> action instanceof UseCardAction || action instanceof UpdateOnlyUseCardAction)
//                                                                  .map(action -> {
//                                                                      if (action instanceof UseCardAction) {
//                                                                          UseCardAction cast = (UseCardAction) action;
//                                                                          return new UseCardActionState(cast);
//                                                                      } else if (action instanceof UpdateOnlyUseCardAction) {
//                                                                          UpdateOnlyUseCardAction cast = (UpdateOnlyUseCardAction) action;
//                                                                          return new UseCardActionState(cast);
//                                                                      }
//                                                                      return null;
//                                                                  })
//                                                                  .collect(Collectors
//                                                                          .toCollection(ArrayList::new));

            if (actionQueue.isEmpty()) {
                throw new IllegalStateException("this shouldn't happen " + AbstractDungeon.actionManager.actions);
            }


//            queueItemState = new CardQueueItemState(AbstractDungeon.actionManager.cardQueue.get(0));
        } else {
            actionState = null;
            actionQueue = null;
//            queueItemState = null;
        }
    }

    public void loadHandSelectScreenState() {
        if (hoveredCard != null) {
            AbstractDungeon.handCardSelectScreen.hoveredCard = hoveredCard.loadCard();
        } else {
            AbstractDungeon.handCardSelectScreen.hoveredCard = null;
        }
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
//            AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = false;
            AbstractDungeon.actionManager.currentAction = actionState.loadAction();
            AbstractDungeon.actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;

            AbstractDungeon.actionManager.actions.clear();
            actionQueue.forEach(action -> AbstractDungeon.actionManager.actions.add(action
                    .loadAction()));

            if (AbstractDungeon.actionManager.actions.isEmpty()) {
                throw new IllegalStateException("this too shouldn't happen");
            }

//            queueItemState.loadQueueItem();
//            AbstractDungeon.actionManager.cardQueue.clear();

//            AbstractDungeon.actionManager.cardQueue.add(queueItemState.loadQueueItem());
        }
    }
}
