package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Mugger;

public class MuggerState extends MonsterState {
    public MuggerState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.MUGGER.ordinal();
    }

    public MuggerState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.MUGGER.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Mugger result = new Mugger(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
