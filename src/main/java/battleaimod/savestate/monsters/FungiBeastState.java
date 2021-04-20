package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.FungiBeast;

public class FungiBeastState extends MonsterState {
    public FungiBeastState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.FUNGI_BEST.ordinal();
    }

    public FungiBeastState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.FUNGI_BEST.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        FungiBeast result = new FungiBeast(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
