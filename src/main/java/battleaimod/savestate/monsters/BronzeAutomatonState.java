package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BronzeAutomaton;

public class BronzeAutomatonState extends MonsterState {
    public BronzeAutomatonState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.BRONZE_AUTOMATON.ordinal();
    }

    public BronzeAutomatonState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.BRONZE_AUTOMATON.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        BronzeAutomaton result = new BronzeAutomaton();
        populateSharedFields(result);
        return result;
    }
}
