package savestate;

import com.megacrit.cardcrawl.relics.AbstractRelic;

public class RelicState {
    private final AbstractRelic relic;

    private final int counter;

    public RelicState(AbstractRelic relic) {
        this.relic = relic;
        this.counter = relic.counter;
    }

    public AbstractRelic loadRelic() {
        relic.counter = counter;
        return relic;
    }
}
