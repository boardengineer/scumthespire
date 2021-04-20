package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Healer;

public class HealerState extends MonsterState {
    public HealerState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.HEALER.ordinal();
    }

    public HealerState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.HEALER.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Healer result = new Healer(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
