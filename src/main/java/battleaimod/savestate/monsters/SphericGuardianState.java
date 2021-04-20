package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.SphericGuardian;

public class SphericGuardianState extends MonsterState {
    public SphericGuardianState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SPHERIC_GUARDIAN.ordinal();
    }

    public SphericGuardianState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SPHERIC_GUARDIAN.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SphericGuardian result = new SphericGuardian(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
