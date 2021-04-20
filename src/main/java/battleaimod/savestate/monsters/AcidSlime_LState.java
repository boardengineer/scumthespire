package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.AcidSlime_L;

public class AcidSlime_LState extends MonsterState {
    public AcidSlime_LState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.ACID_SLIME_L.ordinal();
    }

    public AcidSlime_LState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.ACID_SLIME_L.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        AcidSlime_L result = new AcidSlime_L(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
