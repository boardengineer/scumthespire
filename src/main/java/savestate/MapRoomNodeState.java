package savestate;

import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MapRoomNodeState {
    private final AbstractRoom room;
    private final MapRoomNode mapRoomNode;
    private final boolean taken;
    private final boolean highlighted;
    private final boolean hasEmeraldKey;
    ArrayList<AbstractMonster> monsters = null;
    ArrayList<MonsterState> monsterData = null;
    private ArrayList<AbstractRelic> relics;
    private ArrayList<RewardItem> rewards;
    private ArrayList<MapRoomNodeState> parentLoaders;
    private AbstractRoom.RoomPhase phase;
    private AbstractEvent event;
    private float rewardPopOutTimer;
    private boolean isBattleOver;
    private boolean cannotLose;
    private boolean eliteTrigger;
    private boolean mugged;
    private boolean combatEvent;
    private boolean rewardAllowed;
    private boolean rewardTime;
    private boolean skipMonsterTurn;
    private int baseRareCardChance;
    private int baseUncommonCardChance;
    private int rareCardChance;
    private int uncommonCardChance;
    private float waitTimer;
    private ArrayList<AbstractPotion> potions = null;

    public MapRoomNodeState(MapRoomNode mapRoomNode) {
        this.taken = mapRoomNode.taken;
        this.highlighted = mapRoomNode.highlighted;
        this.hasEmeraldKey = mapRoomNode.hasEmeraldKey;

        this.mapRoomNode = mapRoomNode;
        this.room = mapRoomNode.room;

        if (room == null) {
            return;
        }

        if (room.potions != null) {
            this.potions = (ArrayList<AbstractPotion>) room.potions.clone();
        }
        this.relics = (ArrayList<AbstractRelic>) room.relics.clone();
        this.rewards = (ArrayList<RewardItem>) room.rewards.clone();

        this.phase = room.phase;
        this.event = room.event;

        // rooms that haven't been entered have null Monster groups
        if (room.monsters != null) {
            this.monsterData = room.monsters.monsters.stream()
                                                     .map(monster -> new MonsterState(monster))
                                                     .collect(Collectors
                                                             .toCollection(ArrayList::new));

            this.monsters = (ArrayList<AbstractMonster>) room.monsters.monsters.clone();
        }
        this.rewardPopOutTimer = room.rewardPopOutTimer;
        this.isBattleOver = room.isBattleOver;
        this.cannotLose = room.cannotLose;
        this.eliteTrigger = room.eliteTrigger;
        this.mugged = room.mugged;
        this.combatEvent = room.combatEvent;
        this.rewardAllowed = room.rewardAllowed;
        this.rewardTime = room.rewardTime;
        this.skipMonsterTurn = room.skipMonsterTurn;
        this.baseRareCardChance = room.baseRareCardChance;
        this.baseUncommonCardChance = room.baseUncommonCardChance;
        this.rareCardChance = room.rareCardChance;
        this.uncommonCardChance = room.uncommonCardChance;
        this.waitTimer = AbstractRoom.waitTimer;

//        this.parentLoaders = mapRoomNode.getParents().stream().map(MapRoomNodeLoader::new)
//                                        .collect(Collectors.toCollection(ArrayList::new));
    }

    public MapRoomNode loadMapRoomNode() {
        mapRoomNode.taken = this.taken;
        mapRoomNode.highlighted = this.highlighted;
        mapRoomNode.hasEmeraldKey = this.hasEmeraldKey;

        if (room == null) {
            return mapRoomNode;
        }
        if (potions != null) {
            room.potions = (ArrayList<AbstractPotion>) this.potions.clone();
        }
        room.relics = (ArrayList<AbstractRelic>) this.relics.clone();
        room.rewards = (ArrayList<RewardItem>) this.rewards.clone();

        room.phase = this.phase;
        room.event = this.event;

        room.rewardPopOutTimer = this.rewardPopOutTimer;
        room.isBattleOver = this.isBattleOver;
        room.cannotLose = this.cannotLose;
        room.eliteTrigger = this.eliteTrigger;
        room.mugged = this.mugged;
        room.combatEvent = this.combatEvent;
        room.rewardAllowed = this.rewardAllowed;
        room.rewardTime = this.rewardTime;
        room.skipMonsterTurn = this.skipMonsterTurn;
        room.baseRareCardChance = this.baseRareCardChance;
        room.baseUncommonCardChance = this.baseUncommonCardChance;
        room.rareCardChance = this.rareCardChance;
        room.uncommonCardChance = this.uncommonCardChance;
        AbstractRoom.waitTimer = this.waitTimer;

        if (monsterData != null) {
            room.monsters.monsters = monsterData.stream().map(MonsterState::loadMonster)
                                                .collect(Collectors
                                                        .toCollection(ArrayList::new));
        } else {
            room.monsters = null;
        }

        if (room.phase == AbstractRoom.RoomPhase.COMBAT) {
            if (room.monsters != null) {
                room.update();
            }
        } else if (room.phase == AbstractRoom.RoomPhase.EVENT) {
            if (room.event != null) {
                room.update();
            }
        }

        return mapRoomNode;
    }

}
