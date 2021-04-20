package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.JawWorm;

public class JawWormState extends MonsterState {
    public JawWormState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.JAWWORM.ordinal();
    }

    public JawWormState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.JAWWORM.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        JawWorm result = new JawWorm(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
