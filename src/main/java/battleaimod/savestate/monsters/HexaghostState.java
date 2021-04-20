package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Hexaghost;

public class HexaghostState extends MonsterState {
    public HexaghostState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.HEXAGHOST.ordinal();
    }

    public HexaghostState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.HEXAGHOST.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Hexaghost result = new Hexaghost();
        populateSharedFields(result);
        return result;
    }
}
