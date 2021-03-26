package savestate;

import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class DamageInfoState {
    private final DamageInfo damageInfo;
    private final AbstractCreature owner;
    private final String name;
    private final DamageInfo.DamageType type;
    private final int base;
    private final int output;
    public boolean isModified;

    public DamageInfoState(DamageInfo damageInfo) {
        this.damageInfo = damageInfo;

        this.owner = damageInfo.owner;
        this.name = damageInfo.name;
        this.type = damageInfo.type;
        this.base = damageInfo.base;
        this.output = damageInfo.output;
    }

    public DamageInfo loadDamageInfo() {
        damageInfo.owner = owner;
        damageInfo.name = name;
        damageInfo.type = type;
        damageInfo.base = base;
        damageInfo.output = output;
        damageInfo.isModified = isModified;
        return damageInfo;
    }
}
