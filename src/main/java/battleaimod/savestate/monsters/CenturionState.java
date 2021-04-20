package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Centurion;

public class CenturionState extends MonsterState {
    public CenturionState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.CENTURION.ordinal();
    }

    public CenturionState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.CENTURION.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Centurion result = new Centurion(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
