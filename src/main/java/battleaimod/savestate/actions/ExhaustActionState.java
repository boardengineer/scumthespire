package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ExhaustAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class ExhaustActionState implements CurrentActionState {
    private final boolean isRandom;
    private final boolean anyNumber;
    private final boolean canPickZero;
    private final int amount;

    public ExhaustActionState(AbstractGameAction action) {
        this((ExhaustAction) action);
    }

    public ExhaustActionState(ExhaustAction action) {
        this.isRandom = ReflectionHacks.getPrivate(action, ExhaustAction.class, "isRandom");
        this.anyNumber = ReflectionHacks.getPrivate(action, ExhaustAction.class, "anyNumber");
        this.canPickZero = ReflectionHacks.getPrivate(action, ExhaustAction.class, "canPickZero");
        this.amount = action.amount;
    }

    @Override
    public ExhaustAction loadCurrentAction() {
        ExhaustAction result = new ExhaustAction(AbstractDungeon.player, AbstractDungeon.player, amount, isRandom, anyNumber);

        // This should make the action only trigger the second hald of the update
        ReflectionHacks
                .setPrivate(result, AbstractGameAction.class, "duration", 0);

        return result;
    }

    @SpirePatch(
            clz = ExhaustAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoDoubleExhaustActionPatch {
        public static void Postfix(ExhaustAction _instance) {
            // Force the action to stay in the the manager until cards are selected
            if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved && AbstractDungeon.isScreenUp) {
                _instance.isDone = false;
            }
        }
    }
}
