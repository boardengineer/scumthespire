package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.GremlinLeader;

public class GremlinLeaderState extends MonsterState {
    public GremlinLeaderState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.GREMLIN_LEADER.ordinal();
    }

    public GremlinLeaderState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.GREMLIN_LEADER.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinLeader result = new GremlinLeader();
        populateSharedFields(result);
        return result;
    }
}
