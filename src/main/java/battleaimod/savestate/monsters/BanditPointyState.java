package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BanditPointy;

public class BanditPointyState extends MonsterState {
    public BanditPointyState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.BANDIT_CHILD.ordinal();
    }

    public BanditPointyState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.BANDIT_CHILD.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        BanditPointy result = new BanditPointy(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
