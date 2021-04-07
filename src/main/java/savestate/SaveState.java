package savestate;

import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import communicationmod.CommunicationMod;
import communicationmod.GameStateListener;

import java.util.stream.Collectors;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;
import static communicationmod.patches.MonsterPatch.shouldGoFast;

public class SaveState {
    public MapRoomNode mapNode;
    ListState listLoader;
    int floorNum;

    PlayerState playerLoader;
    public MapRoomNodeState roomLoader;
    RngState rngLoader;
    AbstractDungeon.CurrentScreen screen;

    AbstractDungeon.CurrentScreen previousScreen = null;
    boolean previousScreenUp = false;
    AbstractRoom.RoomPhase previousPhase = null;
    boolean myTurn = false;
    public int turn;
    private MapRoomNodeState curMapNodeState;

    public SaveState() {
        if (shouldGoFast()) {
            if (actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS || !actionManager.monsterQueue
                    .isEmpty()) {
                while (actionManager.currentAction != null && !actionManager.currentAction.isDone) {
                    actionManager.currentAction.update();
                }
                actionManager.update();
            }
        }


        this.curMapNodeState = new MapRoomNodeState(AbstractDungeon.currMapNode);
        playerLoader = new PlayerState(AbstractDungeon.player);
        mapNode = AbstractDungeon.currMapNode;
        screen = AbstractDungeon.screen;
        roomLoader = new MapRoomNodeState(mapNode);
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

        curMapNodeState.loadMapRoomNode(AbstractDungeon.currMapNode);
        AbstractDungeon.screen = screen;

        AbstractDungeon.isScreenUp = false;
        listLoader.loadLists();

        AbstractDungeon.dungeonMapScreen.close();

        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.NONE;

        AbstractDungeon.floorNum = floorNum;

        CommunicationMod.readyForUpdate = true;

        CombatRewardScreenState.loadCombatRewardScreen();

        rngLoader.loadRng();

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

    public String getDedupeString() {
        String monsterHealths = roomLoader.monsterData.stream().map(monster -> String
                .format("%03d", monster.currentHealth)).collect(Collectors.joining());

        return String
                .format("%03d%s%03d%s", playerLoader.currentHealth, monsterHealths, turn, getPlayerHand());
    }

    public int getNumSlimes() {
        return playerLoader.getNumSlimes();
    }
}
