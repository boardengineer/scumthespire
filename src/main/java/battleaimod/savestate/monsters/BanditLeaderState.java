package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BanditLeader;

public class BanditLeaderState extends MonsterState {
    public BanditLeaderState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.BANDIT_LEADER.ordinal();
    }

    public BanditLeaderState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.BANDIT_LEADER.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        BanditLeader result = new BanditLeader(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
