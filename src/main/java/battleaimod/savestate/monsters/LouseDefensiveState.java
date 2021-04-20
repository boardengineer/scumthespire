package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.LouseDefensive;

public class LouseDefensiveState extends MonsterState {
    public LouseDefensiveState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.FUZZY_LOUSE_DEFENSIVE.ordinal();
    }

    public LouseDefensiveState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.FUZZY_LOUSE_DEFENSIVE.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        LouseDefensive result = new LouseDefensive(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
