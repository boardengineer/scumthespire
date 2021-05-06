package battleaimod.savestate.actions;

import battleaimod.fastobjects.actions.DrawCardActionFast;
import battleaimod.fastobjects.actions.UpdateOnlyUseCardAction;
import battleaimod.savestate.SetMoveActionState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;

import java.util.function.Function;

public enum Action {
    APPLY_POWER_ACTION(ApplyPowerAction.class, action -> new ApplyPowerActionState(action)),
    CHANGE_STATE_ACTION(ChangeStateAction.class, action -> new ChangeStateActionState(action)),
    DAMAGE_ACTION(DamageAction.class, action -> new DamageActionState(action)),
    DAMAGE_ALL_ENEMIES_ACTION(DamageAllEnemiesAction.class, action -> new DamageAllEnemiesActionState(action)),
    DAMAGE_RANDOM_ENEMY_ACTION(DamageRandomEnemyAction.class, action -> new DamageRandomEnemyActionState(action)),
    DRAW_CARD_ACTION(DrawCardAction.class, action -> new DrawCardActionState(action)),
    DRAW_CARD_ACTION_FAST(DrawCardActionFast.class, action -> new DrawCardActionState(action)),
    ENABLE_END_TURN_BUTTON_ACTION(EnableEndTurnButtonAction.class, action -> new EnableEndTurnButtonActionState(action)),
    ESCAPE_ACTION(EscapeAction.class, action -> new EscapeActionState(action)),
    GAIN_BLOCK_ACTION(GainBlockAction.class, action -> new GainBlockActionState(action)),
    GAIN_ENERGY_ACTION(GainEnergyAction.class, action -> new GainEnergyActionState(action)),
    LOSE_HP_ACTION(LoseHPAction.class, action -> new LoseHPActionState(action)),
    MAKE_TEMP_CARD_IN_DRAW_PILE_ACTION(MakeTempCardInDrawPileAction.class, action -> new MakeTempCardInDrawPileActionState(action)),
    REDUCE_POWER_ACTION(ReducePowerAction.class, action -> new ReducePowerActionState(action)),
    REMOVE_SPECIFIC_POWER_ACTION(RemoveSpecificPowerAction.class, action -> new RemoveSpecificPowerActionState(action)),
    ROLL_MOVE_ACTION(RollMoveAction.class, action -> new RollMoveActionState(action)),
    SET_MOVE_ACTION(SetMoveAction.class, action -> new SetMoveActionState(action)),
    UPDATE_ONLY_USE_CARD_ACTION(UpdateOnlyUseCardAction.class, action -> new UseCardActionState((UpdateOnlyUseCardAction) action)),
    USE_CARD_ACTION(UseCardAction.class, action -> new UseCardActionState((UseCardAction) action));

    public Function<AbstractGameAction, ActionState> factory;
    public Class<? extends AbstractGameAction> actionClass;

    Action() {
    }

    Action(Function<AbstractGameAction, ActionState> factory) {
        this.factory = factory;
    }

    Action(Class<? extends AbstractGameAction> actionClass, Function<AbstractGameAction, ActionState> factory) {
        this.factory = factory;
        this.actionClass = actionClass;
    }

}
