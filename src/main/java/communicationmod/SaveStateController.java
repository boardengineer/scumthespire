package communicationmod;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;

import java.util.ArrayList;
import java.util.Stack;
import java.util.stream.Collectors;

public class SaveStateController {
    public static Stack<SaveStateController> saveStates = null;

    MapRoomNode mapNode;
    ArrayList<ArrayList<MapRoomNodeLoader>> roomNodeLoaders;
    ListLoader listLoader;
    int floorNum;

    PlayerLoader playerLoader;
    MapRoomNodeLoader roomLoader;
    RngLoader rngLoader;
    CombatRewardScreenLoader combatRewardScreenLoader;
    AbstractDungeon.CurrentScreen screen;

    public void saveState() {
        playerLoader = new PlayerLoader(AbstractDungeon.player);
        roomNodeLoaders = AbstractDungeon.map.stream()
                                             .map(list -> list.stream().map(MapRoomNodeLoader::new)
                                                              .collect(Collectors
                                                                      .toCollection(ArrayList::new)))
                                             .collect(Collectors.toCollection(ArrayList::new));
        mapNode = AbstractDungeon.currMapNode;
        screen = AbstractDungeon.screen;
        roomLoader = new MapRoomNodeLoader(mapNode);
        combatRewardScreenLoader = new CombatRewardScreenLoader(AbstractDungeon.combatRewardScreen);
        rngLoader = new RngLoader();
        listLoader = new ListLoader();
        floorNum = AbstractDungeon.floorNum;

        if (SaveStateController.saveStates == null) {
            SaveStateController.saveStates = new Stack<>();
        }

        saveStates.push(this);
    }

    public void loadState() {
        AbstractDungeon.player = playerLoader.loadPlayer();

        AbstractDungeon.currMapNode = mapNode;
        AbstractDungeon.screen = screen;

        AbstractDungeon.isScreenUp = false;
        listLoader.loadLists();

        AbstractDungeon.dungeonMapScreen.close();

        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.NONE;

        AbstractDungeon.floorNum = floorNum;

        AbstractDungeon.map = roomNodeLoaders.stream()
                                             .map(list -> list.stream()
                                                              .map(MapRoomNodeLoader::loadMapRoomNode)
                                                              .collect(Collectors
                                                                      .toCollection(ArrayList::new)))
                                             .collect(Collectors.toCollection(ArrayList::new));

        rngLoader.loadRng();
        AbstractDungeon.combatRewardScreen = combatRewardScreenLoader.loadCombatRewardScreen();
    }
}
