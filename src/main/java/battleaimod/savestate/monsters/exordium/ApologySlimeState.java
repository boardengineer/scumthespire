package battleaimod.savestate.monsters.exordium;

import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.ApologySlime;

public class ApologySlimeState extends MonsterState {
    public ApologySlimeState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.APOLOGY_SLIME.ordinal();
    }

    public ApologySlimeState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.APOLOGY_SLIME.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        ApologySlime result = new ApologySlime();
        populateSharedFields(result);
        return result;
    }
}
