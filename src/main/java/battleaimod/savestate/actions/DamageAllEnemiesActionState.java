package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class DamageAllEnemiesActionState implements ActionState {
    private final int[] damage;
    private final DamageInfo.DamageType type;
    private final AbstractGameAction.AttackEffect effect;

    public DamageAllEnemiesActionState(AbstractGameAction action) {
        this((DamageAllEnemiesAction) action);
    }

    public DamageAllEnemiesActionState(DamageAllEnemiesAction action) {
        damage = ReflectionHacks.getPrivate(action, DamageAllEnemiesAction.class, "damage");
        type = action.damageType;
        effect = action.attackEffect;
    }

    @Override
    public AbstractGameAction loadAction() {
        return new DamageAllEnemiesAction(AbstractDungeon.player, damage, type, effect);
    }
}
