package battleaimod.savestate;

import basemod.ReflectionHacks;
import battleaimod.savestate.actions.ActionState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class DamageAllEnemiesActionState implements ActionState
{
    private final int[] damage;
    private final int baseDamage;
    private final boolean firstFrame;
    private final boolean utilizeBaseDamage;
    private final AbstractGameAction.AttackEffect attackEffect;
    private final AbstractCreature source;
    private final DamageInfo.DamageType damageType;

    public DamageAllEnemiesActionState(DamageAllEnemiesAction action) {
        this.damage = action.damage;
        this.baseDamage = ReflectionHacks.getPrivate(action, DamageAllEnemiesAction.class, "baseDamage");
        this.firstFrame = ReflectionHacks.getPrivate(action, DamageAllEnemiesAction.class, "firstFrame");
        this.utilizeBaseDamage = ReflectionHacks.getPrivate(action, DamageAllEnemiesAction.class, "utilizeBaseDamage");
        attackEffect = action.attackEffect;
        source = action.source;
        damageType = action.damageType;
    }

    public DamageAllEnemiesAction loadAction() {
        DamageAllEnemiesAction result;
        if(utilizeBaseDamage)
        {
            result = new DamageAllEnemiesAction((AbstractPlayer) source, baseDamage, damageType, attackEffect);
        }
        else
        {
            result = new DamageAllEnemiesAction(source, damage, damageType, attackEffect, true);
        }
        ReflectionHacks.setPrivate(result, DamageAllEnemiesAction.class, "firstFrame", firstFrame);
        
        return result;
    }
}
