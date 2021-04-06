package savestate;

import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MapRoomNodeState {
    private final AbstractRoom room;
    private final MapRoomNode mapRoomNode;
    private final boolean taken;
    private final boolean highlighted;
    private final boolean hasEmeraldKey;
    public ArrayList<MonsterState> monsterData = null;
    private AbstractRoom.RoomPhase phase;
    private boolean isBattleOver;
    private boolean cannotLose;
    private boolean eliteTrigger;
    private boolean mugged;
    private boolean combatEvent;
    private boolean rewardAllowed;
    private boolean rewardTime;
    private boolean skipMonsterTurn;
    private float waitTimer;

    public MapRoomNodeState(MapRoomNode mapRoomNode) {
        this.taken = mapRoomNode.taken;
        this.highlighted = mapRoomNode.highlighted;
        this.hasEmeraldKey = mapRoomNode.hasEmeraldKey;

        this.mapRoomNode = mapRoomNode;
        this.room = mapRoomNode.room;
        VulnerablePower v;

        if (room == null) {
            return;
        }

        this.phase = room.phase;

        // rooms that haven't been entered have null Monster groups
        if (room.monsters != null) {
            this.monsterData = room.monsters.monsters.stream()
                                                     .map(monster -> new MonsterState(monster))
                                                     .collect(Collectors
                                                             .toCollection(ArrayList::new));
        }
        this.isBattleOver = room.isBattleOver;
        this.cannotLose = room.cannotLose;
        this.eliteTrigger = room.eliteTrigger;
        this.mugged = room.mugged;
        this.combatEvent = room.combatEvent;
        this.rewardAllowed = room.rewardAllowed;
        this.rewardTime = room.rewardTime;
        this.skipMonsterTurn = room.skipMonsterTurn;
        this.waitTimer = AbstractRoom.waitTimer;
    }

    public MapRoomNode loadMapRoomNode() {
        mapRoomNode.taken = this.taken;
        mapRoomNode.highlighted = this.highlighted;
        mapRoomNode.hasEmeraldKey = this.hasEmeraldKey;

        if (room == null) {
            return mapRoomNode;
        }
        room.phase = this.phase;

        room.isBattleOver = this.isBattleOver;
        room.cannotLose = this.cannotLose;
        room.eliteTrigger = this.eliteTrigger;
        room.mugged = this.mugged;
        room.combatEvent = this.combatEvent;
        room.rewardAllowed = this.rewardAllowed;
        room.rewardTime = this.rewardTime;
        room.skipMonsterTurn = this.skipMonsterTurn;
        AbstractRoom.waitTimer = this.waitTimer;

        if (monsterData != null) {
            room.monsters = new MonsterGroup(monsterData.stream().map(MonsterState::loadMonster)
                                                        .toArray(AbstractMonster[]::new));
        } else {
            room.monsters = null;
        }

        if (room.phase == AbstractRoom.RoomPhase.COMBAT) {
            if (room.monsters != null) {
            }
        } else if (room.phase == AbstractRoom.RoomPhase.EVENT) {
            if (room.event != null) {
                room.update();
            }
        }

        return mapRoomNode;
    }

}
