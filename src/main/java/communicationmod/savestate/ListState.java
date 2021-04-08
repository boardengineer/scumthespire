package communicationmod.savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListState {
    private static final String ITEM_DELIMETER = "!;!";

    private final ArrayList<String> commonRelicPool;
    private final ArrayList<String> uncommonRelicPool;
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
        this.commonRelicPool = (ArrayList<String>) AbstractDungeon.commonRelicPool.clone();
        this.uncommonRelicPool = (ArrayList<String>) AbstractDungeon.uncommonRelicPool.clone();
        this.rareRelicPool = (ArrayList<String>) AbstractDungeon.rareRelicPool.clone();
        this.shopRelicPool = (ArrayList<String>) AbstractDungeon.shopRelicPool.clone();
        this.bossRelicPool = (ArrayList<String>) AbstractDungeon.bossRelicPool.clone();
        this.monsterList = (ArrayList<String>) AbstractDungeon.monsterList.clone();
        this.eliteMonsterList = (ArrayList<String>) AbstractDungeon.eliteMonsterList.clone();
        this.bossList = (ArrayList<String>) AbstractDungeon.bossList.clone();
        this.eventList = (ArrayList<String>) AbstractDungeon.eventList.clone();
        this.shrineList = (ArrayList<String>) AbstractDungeon.shrineList.clone();
        this.specialOneTimeEventList = (ArrayList<String>) AbstractDungeon.specialOneTimeEventList
                .clone();
    }

    public ListState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.commonRelicPool = getList(parsed.get("common_relic_pool").getAsString());
        this.uncommonRelicPool = getList(parsed.get("uncommon_relic_pool").getAsString());
        this.rareRelicPool = getList(parsed.get("rare_relic_pool").getAsString());
        this.shopRelicPool = getList(parsed.get("shop_relic_pool").getAsString());
        this.bossRelicPool = getList(parsed.get("boss_relic_pool").getAsString());
        this.monsterList = getList(parsed.get("monster_list").getAsString());
        this.eliteMonsterList = getList(parsed.get("elite_monster_list").getAsString());
        this.bossList = getList(parsed.get("boss_list").getAsString());
        this.eventList = getList(parsed.get("event_list").getAsString());
        this.shrineList = getList(parsed.get("shrine_list").getAsString());
        this.specialOneTimeEventList = getList(parsed.get("special_one_time_event_list")
                                                     .getAsString());
    }

    public void loadLists() {
        AbstractDungeon.commonRelicPool = (ArrayList<String>) commonRelicPool.clone();
        AbstractDungeon.uncommonRelicPool = (ArrayList<String>) uncommonRelicPool.clone();
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

    public String encode() {
        JsonObject listStateJson = new JsonObject();

        listStateJson.addProperty("common_relic_pool", getListString(commonRelicPool));
        listStateJson.addProperty("uncommon_relic_pool", getListString(uncommonRelicPool));
        listStateJson.addProperty("rare_relic_pool", getListString(rareRelicPool));
        listStateJson.addProperty("shop_relic_pool", getListString(shopRelicPool));
        listStateJson.addProperty("boss_relic_pool", getListString(bossRelicPool));
        listStateJson.addProperty("monster_list", getListString(monsterList));
        listStateJson.addProperty("elite_monster_list", getListString(eliteMonsterList));
        listStateJson.addProperty("boss_list", getListString(bossList));
        listStateJson.addProperty("event_list", getListString(eventList));
        listStateJson.addProperty("shrine_list", getListString(shrineList));
        listStateJson
                .addProperty("special_one_time_event_list", getListString(specialOneTimeEventList));

        return listStateJson.toString();
    }

    private static String getListString(ArrayList<String> list) {
        return list.stream().collect(Collectors.joining(ITEM_DELIMETER));
    }

    private static ArrayList<String> getList(String listString) {
        return Stream.of(listString.split(ITEM_DELIMETER)).filter(s -> !s.isEmpty())
                     .collect(Collectors.toCollection(ArrayList::new));
    }
}
