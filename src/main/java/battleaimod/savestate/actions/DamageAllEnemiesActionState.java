package battleaimod.savestate.actions;

import basemod.ReflectionHacks;
import battleaimod.savestate.actions.ActionState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

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

    public DamageAllEnemiesActionState(DamageAllEnemiesAction action) {
        damage = ReflectionHacks.getPrivate(action, DamageAllEnemiesAction.class, "damage");
        type = action.damageType;
        effect = action.attackEffect;
    }

    public DamageAllEnemiesAction loadAction() {
        DamageAllEnemiesAction result;
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
