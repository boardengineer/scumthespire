package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Champ;

public class ChampState extends MonsterState {
    public ChampState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.CHAMP.ordinal();
    }

    public ChampState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.CHAMP.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Champ result = new Champ();
        populateSharedFields(result);
        return result;
    }
}
