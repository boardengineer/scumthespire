package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.ShelledParasite;

public class ShelledParasiteState extends MonsterState {
    public ShelledParasiteState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SHELLED_PARASITE.ordinal();
    }

    public ShelledParasiteState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SHELLED_PARASITE.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        ShelledParasite result = new ShelledParasite(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
