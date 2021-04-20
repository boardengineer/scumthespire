package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Cultist;

public class CultistState extends MonsterState {
    public CultistState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.CULTIST.ordinal();
    }

    public CultistState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.CULTIST.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Cultist result = new Cultist(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
