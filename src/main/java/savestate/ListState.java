package savestate;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.ArrayList;

public class ListState {
    private final ArrayList<String> commonRelicPool;
    private final ArrayList<String> rareRelicPool;
    private final ArrayList<String> shopRelicPool;
    private final ArrayList<String> bossRelicPool;
    private final ArrayList<String> monsterList;
    private final ArrayList<String> eliteMonsterList;
    private final ArrayList<String> bossList;
    private final ArrayList<String> eventList;
    private final ArrayList<String> shrineList;
    private final ArrayList<String> specialOneTimeEventList;

    public ListState() {
        commonRelicPool = (ArrayList<String>) AbstractDungeon.commonRelicPool.clone();
        rareRelicPool = (ArrayList<String>) AbstractDungeon.rareRelicPool.clone();
        shopRelicPool = (ArrayList<String>) AbstractDungeon.shopRelicPool.clone();
        bossRelicPool = (ArrayList<String>) AbstractDungeon.bossRelicPool.clone();
        monsterList = (ArrayList<String>) AbstractDungeon.monsterList.clone();
        eliteMonsterList = (ArrayList<String>) AbstractDungeon.eliteMonsterList.clone();
        bossList = (ArrayList<String>) AbstractDungeon.bossList.clone();
        eventList = (ArrayList<String>) AbstractDungeon.eventList.clone();
        shrineList = (ArrayList<String>) AbstractDungeon.shrineList.clone();
        specialOneTimeEventList = (ArrayList<String>) AbstractDungeon.specialOneTimeEventList
                .clone();
    }

    public void loadLists() {
        AbstractDungeon.commonRelicPool = (ArrayList<String>) commonRelicPool.clone();
        AbstractDungeon.rareRelicPool = (ArrayList<String>) rareRelicPool.clone();
        AbstractDungeon.shopRelicPool = (ArrayList<String>) shopRelicPool.clone();
        AbstractDungeon.bossRelicPool = (ArrayList<String>) bossRelicPool.clone();
        AbstractDungeon.monsterList = (ArrayList<String>) monsterList.clone();
        AbstractDungeon.eliteMonsterList = (ArrayList<String>) eliteMonsterList.clone();
        AbstractDungeon.bossList = (ArrayList<String>) bossList.clone();
        AbstractDungeon.eventList = (ArrayList<String>) eventList.clone();
        AbstractDungeon.shrineList = (ArrayList<String>) shrineList.clone();
        AbstractDungeon.specialOneTimeEventList = (ArrayList<String>) specialOneTimeEventList
                .clone();
    }
}
