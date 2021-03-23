package communicationmod;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.random.Random;

public class RngLoader {
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

    public RngLoader() {
        monsterRng = AbstractDungeon.monsterRng.copy();
        mapRng = AbstractDungeon.mapRng.copy();
        eventRng = AbstractDungeon.eventRng.copy();
        merchantRng = AbstractDungeon.merchantRng.copy();
        cardRng = AbstractDungeon.cardRng.copy();
        treasureRng = AbstractDungeon.treasureRng.copy();
        relicRng = AbstractDungeon.relicRng.copy();
        potionRng = AbstractDungeon.potionRng.copy();
        monsterHpRng = AbstractDungeon.monsterHpRng.copy();
        aiRng = AbstractDungeon.aiRng.copy();
        shuffleRng = AbstractDungeon.shuffleRng.copy();
        cardRandomRng = AbstractDungeon.cardRandomRng.copy();
        miscRng = AbstractDungeon.miscRng.copy();
    }

    public void loadRng() {
        AbstractDungeon.monsterRng = monsterRng.copy();
        AbstractDungeon.mapRng = mapRng.copy();
        AbstractDungeon.eventRng = eventRng.copy();
        AbstractDungeon.merchantRng = merchantRng.copy();
        AbstractDungeon.cardRng = cardRng.copy();
        AbstractDungeon.treasureRng = treasureRng.copy();
        AbstractDungeon.relicRng = relicRng.copy();
        AbstractDungeon.potionRng = potionRng.copy();
        AbstractDungeon.monsterHpRng = monsterHpRng.copy();
        AbstractDungeon.aiRng = aiRng.copy();
        AbstractDungeon.shuffleRng = shuffleRng.copy();
        AbstractDungeon.cardRandomRng = cardRandomRng.copy();
        AbstractDungeon.miscRng = miscRng.copy();
    }
}
