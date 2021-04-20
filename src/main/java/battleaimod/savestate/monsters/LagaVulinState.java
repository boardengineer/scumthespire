package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Lagavulin;

public class LagaVulinState extends MonsterState {
    public LagaVulinState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.LAGAVULIN.ordinal();
    }

    public LagaVulinState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.LAGAVULIN.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Lagavulin result = new Lagavulin(lagavulinIsAsleep);

        populateSharedFields(result);
        return result;
    }
}
