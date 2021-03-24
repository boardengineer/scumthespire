package savestate;

import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.EventHelper;
import com.megacrit.cardcrawl.random.Random;

import java.util.ArrayList;

public class RngState {
    private final Random monsterRng;
    private final Random mapRng;
    private final Random eventRng;
    private final Random merchantRng;
    private final Random cardRng;
    private final Random treasureRng;
    private final Random relicRng;
    private final Random potionRng;
    private final Random monsterHpRng;
    private final Random aiRng;
    private final Random shuffleRng;
    private final Random cardRandomRng;
    private final Random miscRng;

    private final ArrayList<Float> eventHelperChances;

    public RngState() {
        monsterRng = new Random(Settings.seed, AbstractDungeon.monsterRng.counter);
        mapRng = new Random(Settings.seed, AbstractDungeon.mapRng.counter);
        eventRng = new Random(Settings.seed, AbstractDungeon.eventRng.counter);
        merchantRng = new Random(Settings.seed, AbstractDungeon.merchantRng.counter);
        cardRng = new Random(Settings.seed, AbstractDungeon.cardRng.counter);
        treasureRng = new Random(Settings.seed, AbstractDungeon.treasureRng.counter);
        relicRng = new Random(Settings.seed, AbstractDungeon.relicRng.counter);
        potionRng = new Random(Settings.seed, AbstractDungeon.potionRng.counter);
        monsterHpRng = new Random(Settings.seed, AbstractDungeon.monsterHpRng.counter);
        aiRng = new Random(Settings.seed, AbstractDungeon.aiRng.counter);
        shuffleRng = new Random(Settings.seed, AbstractDungeon.shuffleRng.counter);
        cardRandomRng = new Random(Settings.seed, AbstractDungeon.cardRandomRng.counter);
        miscRng = new Random(Settings.seed, AbstractDungeon.miscRng.counter);
        eventHelperChances = EventHelper.getChances();
    }

    public void loadRng() {
        AbstractDungeon.monsterRng = new Random(Settings.seed, monsterRng.counter);
        AbstractDungeon.mapRng = new Random(Settings.seed, mapRng.counter);
        AbstractDungeon.eventRng = new Random(Settings.seed, eventRng.counter);
        AbstractDungeon.merchantRng = new Random(Settings.seed, merchantRng.counter);
        AbstractDungeon.cardRng = new Random(Settings.seed, cardRng.counter);
        AbstractDungeon.treasureRng = new Random(Settings.seed, treasureRng.counter);
        AbstractDungeon.relicRng = new Random(Settings.seed, relicRng.counter);
        AbstractDungeon.potionRng = new Random(Settings.seed, potionRng.counter);
        AbstractDungeon.monsterHpRng = new Random(Settings.seed, monsterHpRng.counter);
        AbstractDungeon.aiRng = new Random(Settings.seed, aiRng.counter);
        AbstractDungeon.shuffleRng = new Random(Settings.seed, shuffleRng.counter);
        AbstractDungeon.cardRandomRng = new Random(Settings.seed, cardRandomRng.counter);
        AbstractDungeon.miscRng = new Random(Settings.seed, miscRng.counter);
        EventHelper.setChances(eventHelperChances);
    }
}
