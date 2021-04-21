package battleaimod.savestate.monsters.exordium;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinFat;

public class GremlinFatState extends MonsterState {
    public GremlinFatState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.GREMLIN_FAT.ordinal();
    }

    public GremlinFatState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.GREMLIN_FAT.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinFat result = new GremlinFat(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
