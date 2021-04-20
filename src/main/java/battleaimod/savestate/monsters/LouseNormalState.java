package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.LouseNormal;

public class LouseNormalState extends MonsterState {
    public LouseNormalState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.FUZZY_LOUSE_NORMAL.ordinal();
    }

    public LouseNormalState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.FUZZY_LOUSE_NORMAL.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        LouseNormal result = new LouseNormal(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
