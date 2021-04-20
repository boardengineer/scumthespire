package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.AcidSlime_S;

public class AcidSlime_SState extends MonsterState {
    public AcidSlime_SState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.ACID_SLIME_S.ordinal();
    }

    public AcidSlime_SState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.ACID_SLIME_S.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        AcidSlime_S result = new AcidSlime_S(offsetX, offsetY, 0);
        populateSharedFields(result);
        return result;
    }
}
