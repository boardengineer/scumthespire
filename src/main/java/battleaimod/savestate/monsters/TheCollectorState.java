package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.TheCollector;

public class TheCollectorState extends MonsterState {
    public TheCollectorState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.THE_COLLECTOR.ordinal();
    }

    public TheCollectorState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.THE_COLLECTOR.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        TheCollector result = new TheCollector();
        populateSharedFields(result);
        return result;
    }
}
