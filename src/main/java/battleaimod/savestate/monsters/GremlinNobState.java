package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinNob;

public class GremlinNobState extends MonsterState {
    public GremlinNobState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.GREMLIN_NOB.ordinal();
    }

    public GremlinNobState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.GREMLIN_NOB.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinNob result = new GremlinNob(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
