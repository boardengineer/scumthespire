package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class DamageAllEnemiesActionState implements ActionState {
    private final int[] damage;
    private final DamageInfo.DamageType type;
    private final AbstractGameAction.AttackEffect effect;
    private final int baseDamage;
    private final boolean utilizeBaseDamage;
    private final AbstractCreature source;

    public DamageAllEnemiesActionState(AbstractGameAction action) {
        this((DamageAllEnemiesAction) action);
    }

    public DamageAllEnemiesActionState(DamageAllEnemiesAction action) {
        damage = ReflectionHacks.getPrivate(action, DamageAllEnemiesAction.class, "damage");
        this.baseDamage = ReflectionHacks.getPrivate(action, DamageAllEnemiesAction.class, "baseDamage");
        this.utilizeBaseDamage = ReflectionHacks.getPrivate(action, DamageAllEnemiesAction.class, "utilizeBaseDamage");
        type = action.damageType;
        effect = action.attackEffect;
        source = action.source;
    }

    @Override
    public DamageAllEnemiesAction loadAction() {
        if(utilizeBaseDamage)
        {
            return new DamageAllEnemiesAction((AbstractPlayer) source, baseDamage, type, effect);
        }
        else
        {
            return new DamageAllEnemiesAction(source, damage, type, effect, true);
        }
    }
}
