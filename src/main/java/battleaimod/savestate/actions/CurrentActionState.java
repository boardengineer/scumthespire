package battleaimod.savestate.actions;

import battleaimod.BattleAiMod;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;

public interface CurrentActionState {
    AbstractGameAction loadCurrentAction();

    static CurrentActionState getCurrentActionState() {
        if (BattleAiMod.currentActionByClassMap
                .containsKey(actionManager.currentAction.getClass())) {
            return BattleAiMod.currentActionByClassMap
                    .get(actionManager.currentAction.getClass()).factory
                    .apply(actionManager.currentAction);
        } else {
            throw new IllegalStateException("No State Factory for Current Action " + AbstractDungeon.actionManager.currentAction);
        }
    }
}
