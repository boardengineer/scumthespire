package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Sentry;

public class SentryState extends MonsterState {
    public SentryState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SENTRY.ordinal();
    }

    public SentryState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SENTRY.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Sentry result = new Sentry(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
