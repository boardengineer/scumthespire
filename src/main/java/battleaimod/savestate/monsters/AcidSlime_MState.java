package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.AcidSlime_M;

public class AcidSlime_MState extends MonsterState {
    public AcidSlime_MState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.ACID_SLIME_M.ordinal();
    }

    public AcidSlime_MState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.ACID_SLIME_M.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        AcidSlime_M result = new AcidSlime_M(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
