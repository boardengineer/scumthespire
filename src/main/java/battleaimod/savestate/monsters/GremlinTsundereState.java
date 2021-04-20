package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinTsundere;

public class GremlinTsundereState extends MonsterState {
    public GremlinTsundereState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.GREMLIN_TSUNDERE.ordinal();
    }

    public GremlinTsundereState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.GREMLIN_TSUNDERE.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinTsundere result = new GremlinTsundere(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
