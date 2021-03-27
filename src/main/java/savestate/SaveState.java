package savestate;

import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import communicationmod.CommunicationMod;
import communicationmod.GameStateListener;

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

    AbstractDungeon.CurrentScreen previousScreen = null;
    boolean previousScreenUp = false;
    AbstractRoom.RoomPhase previousPhase = null;
    boolean myTurn = false;
    int turn;

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

        this.turn = GameActionManager.turn;
        previousScreen = GameStateListener.previousScreen;
        previousScreenUp = GameStateListener.previousScreenUp;
        previousPhase = GameStateListener.previousPhase;
        myTurn = GameStateListener.myTurn;
    }

    public void loadState() {
        GameActionManager.turn = this.turn;
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
        CommunicationMod.readyForUpdate = true;

        GameStateListener.previousScreen = previousScreen;
        GameStateListener.previousScreenUp = previousScreenUp;
        GameStateListener.previousPhase = previousPhase;
        GameStateListener.myTurn = myTurn;
        GameStateListener.externalChange = true;
    }

    public int getPlayerHealth() {
        return playerLoader.getCurrentHealth();
    }

    public String getPlayerHand() {
        return playerLoader.getHandString();
    }
}
