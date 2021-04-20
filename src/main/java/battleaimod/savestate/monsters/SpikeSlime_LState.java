package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.SpikeSlime_L;

public class SpikeSlime_LState extends MonsterState {
    public SpikeSlime_LState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SPIKE_SLIME_L.ordinal();
    }

    public SpikeSlime_LState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SPIKE_SLIME_L.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SpikeSlime_L result = new SpikeSlime_L(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
