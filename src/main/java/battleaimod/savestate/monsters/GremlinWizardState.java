package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinWizard;

public class GremlinWizardState extends MonsterState {
    public GremlinWizardState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.GREMLIN_WIZARD.ordinal();
    }

    public GremlinWizardState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.GREMLIN_WIZARD.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinWizard result = new GremlinWizard(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
