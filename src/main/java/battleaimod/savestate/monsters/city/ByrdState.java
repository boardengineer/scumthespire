package battleaimod.savestate.monsters.city;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Byrd;

public class ByrdState extends MonsterState {
    public ByrdState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.BYRD.ordinal();
    }

    public ByrdState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.BYRD.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Byrd result = new Byrd(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
