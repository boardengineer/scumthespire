package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinThief;

public class GremlinThiefState extends MonsterState {
    public GremlinThiefState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.GREMLIN_THIEF.ordinal();
    }

    public GremlinThiefState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.GREMLIN_THIEF.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinThief result = new GremlinThief(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
