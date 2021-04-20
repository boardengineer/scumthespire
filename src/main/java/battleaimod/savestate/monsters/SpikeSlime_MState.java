package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.SpikeSlime_M;

public class SpikeSlime_MState extends MonsterState {
    public SpikeSlime_MState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SPIKE_SLIME_M.ordinal();
    }

    public SpikeSlime_MState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SPIKE_SLIME_M.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SpikeSlime_M result = new SpikeSlime_M(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
