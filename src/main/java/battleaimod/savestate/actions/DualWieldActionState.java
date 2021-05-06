package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import battleaimod.savestate.CardState;
import battleaimod.savestate.PlayerState;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.unique.DualWieldAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class DualWieldActionState implements CurrentActionState {
    private final ArrayList<CardState> cannotDuplicate;
    private final int dupeAmount;

    public DualWieldActionState(AbstractGameAction action) {
        this((DualWieldAction) action);
    }

    public DualWieldActionState(DualWieldAction action) {
        ArrayList<AbstractCard> cannotUpgradeSource = ReflectionHacks
                .getPrivate(action, DualWieldAction.class, "cannotDuplicate");
        cannotDuplicate = PlayerState.toCardStateArray(cannotUpgradeSource);

        dupeAmount = ReflectionHacks
                .getPrivate(action, DualWieldAction.class, "dupeAmount");
    }

    @Override
    public DualWieldAction loadCurrentAction() {
        DualWieldAction result = new DualWieldAction(AbstractDungeon.player, dupeAmount);

        ReflectionHacks
                .setPrivate(result, DualWieldAction.class, "cannotDuplicate", cannotDuplicate
                        .stream()
                        .map(CardState::loadCard)
                        .collect(Collectors
                                .toCollection(ArrayList::new)));

        // This should make the action only trigger the second hald of the update
        ReflectionHacks
                .setPrivate(result, AbstractGameAction.class, "duration", 0);

        return result;
    }

    @SpirePatch(
            clz = DualWieldAction.class,
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoFxConstructorPatchOther {
        public static void Postfix(DualWieldAction _instance, AbstractCreature source, int amount) {
            // Duration isn't set correctly if you mess with ACTION_DUR_FAST force it right
            if (shouldGoFast()) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", Settings.ACTION_DUR_FAST);
            }
        }
    }

    @SpirePatch(
            clz = DualWieldAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoDoubleDualWieldPatch {
        public static void Postfix(DualWieldAction _instance) {
            // Force the action to stay in the the manager until cards are selected
            if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved && AbstractDungeon.isScreenUp) {
                _instance.isDone = false;
            }
        }
    }
}
