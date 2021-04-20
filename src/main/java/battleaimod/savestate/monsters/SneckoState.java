package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Snecko;

public class SneckoState extends MonsterState {
    public SneckoState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SNECKO.ordinal();
    }

    public SneckoState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SNECKO.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Snecko result = new Snecko(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
