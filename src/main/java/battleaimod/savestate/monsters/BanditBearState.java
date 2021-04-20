package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BanditBear;

public class BanditBearState extends MonsterState {
    public BanditBearState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.BANDIT_BEAR.ordinal();
    }

    public BanditBearState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.BANDIT_BEAR.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        BanditBear result = new BanditBear(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
