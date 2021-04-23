package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.Darkling;

public class DarklinkState extends MonsterState {
    public DarklinkState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.DARKLING.ordinal();
    }

    public DarklinkState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.DARKLING.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Darkling result = new Darkling(offsetX, offsetY);

        populateSharedFields(result);

        return result;
    }
}
