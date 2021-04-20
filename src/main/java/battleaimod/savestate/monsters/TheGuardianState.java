package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.TheGuardian;

public class TheGuardianState extends MonsterState {
    public TheGuardianState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.THE_GUARDIAN.ordinal();
    }

    public TheGuardianState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.THE_GUARDIAN.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        TheGuardian result = new TheGuardian();
        populateSharedFields(result);
        return result;
    }
}
