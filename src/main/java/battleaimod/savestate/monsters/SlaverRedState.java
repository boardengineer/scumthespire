package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.SlaverRed;

public class SlaverRedState extends MonsterState {
    public SlaverRedState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SLAVER_RED.ordinal();
    }

    public SlaverRedState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SLAVER_RED.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SlaverRed result = new SlaverRed(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
