package savestate;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class SaveState {
    MapRoomNode mapNode;
    ArrayList<ArrayList<MapRoomNodeState>> roomNodeLoaders;
    ListState listLoader;
    int floorNum;

    PlayerState playerLoader;
    MapRoomNodeState roomLoader;
    RngState rngLoader;
    CombatRewardScreenState combatRewardScreenLoader;
    AbstractDungeon.CurrentScreen screen;

    public SaveState() {
        playerLoader = new PlayerState(AbstractDungeon.player);
        roomNodeLoaders = AbstractDungeon.map.stream()
                                             .map(list -> list.stream().map(MapRoomNodeState::new)
                                                              .collect(Collectors
                                                                      .toCollection(ArrayList::new)))
                                             .collect(Collectors.toCollection(ArrayList::new));
        mapNode = AbstractDungeon.currMapNode;
        screen = AbstractDungeon.screen;
        roomLoader = new MapRoomNodeState(mapNode);
        combatRewardScreenLoader = new CombatRewardScreenState(AbstractDungeon.combatRewardScreen);
        rngLoader = new RngState();
        listLoader = new ListState();
        floorNum = AbstractDungeon.floorNum;
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
                                                              .map(MapRoomNodeState::loadMapRoomNode)
                                                              .collect(Collectors
                                                                      .toCollection(ArrayList::new)))
                                             .collect(Collectors.toCollection(ArrayList::new));

        rngLoader.loadRng();
        AbstractDungeon.combatRewardScreen = combatRewardScreenLoader.loadCombatRewardScreen();
    }
}
