package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Chosen;

public class ChosenState extends MonsterState {
    public ChosenState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.CHOSEN.ordinal();
    }

    public ChosenState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.CHOSEN.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Chosen result = new Chosen(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
