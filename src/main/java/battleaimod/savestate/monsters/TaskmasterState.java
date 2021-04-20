package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Taskmaster;

public class TaskmasterState extends MonsterState {
    public TaskmasterState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SLAVER_BOSS.ordinal();
    }

    public TaskmasterState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SLAVER_BOSS.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Taskmaster result = new Taskmaster(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
