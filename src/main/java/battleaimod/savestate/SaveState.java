package battleaimod.savestate;

import basemod.ReflectionHacks;
import battleaimod.BattleAiMod;
import battleaimod.GameStateListener;
import battleaimod.battleai.BattleAiController;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.ConstrictedPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SaveState {
    private final boolean isScreenUp;
    int floorNum;
    boolean previousScreenUp;
    boolean myTurn = false;
    public int turn;
    public String encounterName;

    AbstractRoom.RoomPhase previousPhase = null;
    AbstractDungeon.CurrentScreen screen;
    AbstractDungeon.CurrentScreen previousScreen = null;

//    ListState listState;
    public PlayerState playerState;
    private HandSelectScreenState handSelectScreenState;
    RngState rngState;
    private final int ascensionLevel;

    public MapRoomNodeState curMapNodeState;

    public SaveState() {
        long startSave = System.currentTimeMillis();
        handSelectScreenState = new HandSelectScreenState();

        this.curMapNodeState = new MapRoomNodeState(AbstractDungeon.currMapNode);

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController
                    .addRuntime("Save Time New Save Map Node", System.currentTimeMillis() - startSave);
        }

        long startPlayerSave = System.currentTimeMillis();

        playerState = new PlayerState(AbstractDungeon.player);

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController
                    .addRuntime("Save Time New Save Player", System.currentTimeMillis() - startPlayerSave);
        }

        long startRngLists = System.currentTimeMillis();

        screen = AbstractDungeon.screen;
        rngState = new RngState();
//        listState = new ListState();
        floorNum = AbstractDungeon.floorNum;

        this.turn = GameActionManager.turn;
        previousScreen = GameStateListener.previousScreen;
        previousScreenUp = GameStateListener.previousScreenUp;
        this.isScreenUp = AbstractDungeon.isScreenUp;
        previousPhase = GameStateListener.previousPhase;
        myTurn = GameStateListener.myTurn;
        encounterName = BattleAiController.currentEncounter;
        this.ascensionLevel = AbstractDungeon.ascensionLevel;

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController
                    .addRuntime("Save Time Rng and Lists", System.currentTimeMillis() - startRngLists);
            BattleAiMod.battleAiController
                    .addRuntime("Save Time New Save State", System.currentTimeMillis() - startSave);
        }

    }

    public SaveState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.floorNum = parsed.get("floor_num").getAsInt();
        this.previousScreenUp = parsed.get("previous_screen_up").getAsBoolean();
        this.myTurn = parsed.get("my_turn").getAsBoolean();
        this.turn = parsed.get("turn").getAsInt();

        this.previousPhase = AbstractRoom.RoomPhase
                .valueOf(parsed.get("previous_phase_name").getAsString());

        this.screen = AbstractDungeon.CurrentScreen
                .valueOf(parsed.get("screen_name").getAsString());
        this.previousScreen = AbstractDungeon.CurrentScreen
                .valueOf(parsed.get("previous_screen_name").getAsString());
        this.encounterName = parsed.get("encounter_name").isJsonNull() ? null : parsed
                .get("encounter_name").getAsString();

//        this.listState = new ListState(parsed.get("list_state").getAsString());
        this.playerState = new PlayerState(parsed.get("player_state").getAsString());
        this.rngState = new RngState(parsed.get("rng_state").getAsString());

        this.curMapNodeState = new MapRoomNodeState(parsed.get("cur_map_node_state").getAsString());
        this.isScreenUp = parsed.get("is_screen_up").getAsBoolean();
        this.ascensionLevel = parsed.get("ascension_level").getAsInt();

        // TODO
        handSelectScreenState = null;
    }

    public void loadState() {
        long loadStartTime = System.currentTimeMillis();
//        AbstractDungeon.actionManager.cardQueue.clear();
        AbstractDungeon.actionManager.currentAction = null;
        AbstractDungeon.actionManager.actions.clear();


        AbstractDungeon.ascensionLevel = this.ascensionLevel;
        GameActionManager.turn = this.turn;

        long loadPlayerStartTime = System.currentTimeMillis();

        AbstractDungeon.player = playerState.loadPlayer();

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController
                    .addRuntime("Load Time Player", System
                            .currentTimeMillis() - loadPlayerStartTime);
        }

//        AbstractDungeon.actionManager.cardQueue.get(0)
        CardQueueItem item;
        long point2 = System.currentTimeMillis();
        curMapNodeState.loadMapRoomNode(AbstractDungeon.currMapNode);

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController.roomLoadTime += (System
                    .currentTimeMillis() - point2);
        }

        for (int i = 0; i < playerState.powers.size(); i++) {
            if (playerState.powers.get(i).powerId.equals("Constricted")) {
                AbstractCreature source = AbstractDungeon.getMonsters().monsters
                        .get(playerState.powers.get(i).constrictedSourceIndex);
                ReflectionHacks.setPrivate(AbstractDungeon.player.powers
                        .get(i), ConstrictedPower.class, "source", source);
            }
        }

        AbstractDungeon.screen = screen;

//        AbstractDungeon.isScreenUp = false;
//        listState.loadLists();

        AbstractDungeon.dungeonMapScreen.close();

//        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.NONE;

        AbstractDungeon.floorNum = floorNum;
        AbstractDungeon.isScreenUp = isScreenUp;

        BattleAiMod.readyForUpdate = true;

        if (!shouldGoFast()) {
            CombatRewardScreenState.loadCombatRewardScreen();
        }


        GameStateListener.previousScreen = previousScreen;
        GameStateListener.previousScreenUp = previousScreenUp;
        GameStateListener.previousPhase = previousPhase;
        GameStateListener.myTurn = myTurn;
        GameStateListener.externalChange = true;

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController.loadstateTime += (System
                    .currentTimeMillis() - loadStartTime);
            if (BattleAiMod.battleAiController.runTimes != null) {
                BattleAiMod.battleAiController.addRuntime("Total Load Time", System
                        .currentTimeMillis() - loadStartTime);
            }
        }

        if(handSelectScreenState != null) {
            handSelectScreenState.loadHandSelectScreenState();
        }

        rngState.loadRng();

    }

    public int getPlayerHealth() {
        return playerState.getCurrentHealth();
    }

    public String getPlayerHand() {
        return playerState.getHandString();
    }

    public int getNumSlimes() {
        return playerState.getNumSlimes();
    }

    public int getNumBurns() {
        return playerState.getNumBurns();
    }

    public int getNumInstances(String cardName) {
        return playerState.getNumInstance(cardName);
    }

    public String encode() {
        JsonObject saveStateJson = new JsonObject();

        saveStateJson.addProperty("floor_num", floorNum);
        saveStateJson.addProperty("previous_screen_up", previousScreenUp);
        saveStateJson.addProperty("my_turn", myTurn);
        saveStateJson.addProperty("turn", turn);

        saveStateJson.addProperty("previous_phase_name", previousPhase.name());
        saveStateJson.addProperty("screen_name", screen.name());
        saveStateJson.addProperty("previous_screen_name", previousScreen.name());

//        saveStateJson.addProperty("list_state", listState.encode());
        saveStateJson.addProperty("player_state", playerState.encode());
        saveStateJson.addProperty("rng_state", rngState.encode());

        saveStateJson.addProperty("cur_map_node_state", curMapNodeState.encode());
        saveStateJson.addProperty("encounter_name", encounterName);
        saveStateJson.addProperty("is_screen_up", isScreenUp);
        saveStateJson.addProperty("ascension_level", ascensionLevel);

        return saveStateJson.toString();
    }
}
