package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.OrbWalker;

public class OrbWalkerState extends MonsterState {
    public OrbWalkerState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.ORG_WALKER.ordinal();
    }

    public OrbWalkerState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.ORG_WALKER.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        OrbWalker result = new OrbWalker(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
