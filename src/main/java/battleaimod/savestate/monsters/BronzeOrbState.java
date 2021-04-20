package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BronzeOrb;

public class BronzeOrbState extends MonsterState {
    public BronzeOrbState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.BRONZE_ORB.ordinal();
    }

    public BronzeOrbState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.BRONZE_ORB.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        BronzeOrb result = new BronzeOrb(offsetX, offsetY, bronzeOrbCount);
        populateSharedFields(result);
        return result;
    }
}
