package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.SlimeBoss;

public class SlimeBossState extends MonsterState {
    public SlimeBossState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SLIME_BOSS.ordinal();
    }

    public SlimeBossState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SLIME_BOSS.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SlimeBoss result = new SlimeBoss();
        populateSharedFields(result);
        return result;
    }
}
