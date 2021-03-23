package communicationmod;

import com.megacrit.cardcrawl.relics.AbstractRelic;

public class RelicLoader {
    private final AbstractRelic relic;

    private final int counter;

    public RelicLoader(AbstractRelic relic) {
        this.relic = relic;
        this.counter = relic.counter;
    }

    public AbstractRelic loadRelic() {
        relic.counter = counter;
        return relic;
    }
}
