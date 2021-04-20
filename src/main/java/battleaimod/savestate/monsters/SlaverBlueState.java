package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.SlaverBlue;

public class SlaverBlueState extends MonsterState {
    public SlaverBlueState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SLAVER_BLUE.ordinal();
    }

    public SlaverBlueState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SLAVER_BLUE.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SlaverBlue result = new SlaverBlue(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
