package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinWarrior;

public class GremlinWarriorState extends MonsterState {
    public GremlinWarriorState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.GREMLIN_WARRIOR.ordinal();
    }

    public GremlinWarriorState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.GREMLIN_WARRIOR.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinWarrior result = new GremlinWarrior(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
