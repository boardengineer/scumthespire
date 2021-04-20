package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.SpikeSlime_S;

public class SpikeSlime_SState extends MonsterState {
    public SpikeSlime_SState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SPIKE_SLIME_S.ordinal();
    }

    public SpikeSlime_SState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SPIKE_SLIME_S.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SpikeSlime_S result = new SpikeSlime_S(offsetX, offsetY, 0);
        populateSharedFields(result);
        return result;
    }
}
