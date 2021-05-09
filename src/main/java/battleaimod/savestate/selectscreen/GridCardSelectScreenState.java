package battleaimod.savestate.selectscreen;

import basemod.ReflectionHacks;
import battleaimod.savestate.CardState;
import battleaimod.savestate.PlayerState;
import battleaimod.savestate.actions.ActionState;
import battleaimod.savestate.actions.CurrentActionState;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class GridCardSelectScreenState {
    private final ArrayList<CardState> selectedCards;

    private final CurrentActionState currentActionState;
    private final ArrayList<ActionState> actionQueue;

    // TODO this will probably need to be turned into a State object
    private final CardGroup targetGroup;

    private final boolean isDisabled;
    private final int cardSelectAmount;
    private final int numCards;

    public GridCardSelectScreenState() {
        this.selectedCards = PlayerState
                .toCardStateArray(AbstractDungeon.gridSelectScreen.selectedCards);
        this.targetGroup = AbstractDungeon.gridSelectScreen.targetGroup;
        this.isDisabled = AbstractDungeon.gridSelectScreen.confirmButton.isDisabled;

        this.cardSelectAmount = ReflectionHacks
                .getPrivate(AbstractDungeon.gridSelectScreen, GridCardSelectScreen.class, "cardSelectAmount");
        this.numCards = ReflectionHacks
                .getPrivate(AbstractDungeon.gridSelectScreen, GridCardSelectScreen.class, "numCards");

        if (AbstractDungeon.actionManager.currentAction != null) {
            currentActionState = CurrentActionState.getCurrentActionState();
            actionQueue = ActionState.getActionQueueState();

            if (actionQueue.isEmpty()) {
                throw new IllegalStateException("The action queue shouldn't be empty in the middle of a selection screen");
            }
        } else {
            currentActionState = null;
            actionQueue = null;
        }
    }

    public void loadGridSelectScreen() {
        AbstractDungeon.gridSelectScreen.selectedCards = this.selectedCards.stream()
                                                                           .map(CardState::loadCard)
                                                                           .collect(Collectors
                                                                                   .toCollection(ArrayList::new));
        AbstractDungeon.gridSelectScreen.targetGroup = targetGroup;
        AbstractDungeon.gridSelectScreen.confirmButton.isDisabled = this.isDisabled;

        ReflectionHacks
                .setPrivate(AbstractDungeon.gridSelectScreen, GridCardSelectScreen.class, "cardSelectAmount", cardSelectAmount);

        ReflectionHacks
                .setPrivate(AbstractDungeon.gridSelectScreen, GridCardSelectScreen.class, "numCards", numCards);

        if (currentActionState != null) {
            AbstractDungeon.actionManager.currentAction = currentActionState.loadCurrentAction();
            AbstractDungeon.actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;

            actionQueue.forEach(action -> AbstractDungeon.actionManager.actions.add(action
                    .loadAction()));

            if (AbstractDungeon.actionManager.actions.isEmpty()) {
                throw new IllegalStateException("this too shouldn't happen");
            }
        }
    }
}
