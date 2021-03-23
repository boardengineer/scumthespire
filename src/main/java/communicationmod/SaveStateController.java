package communicationmod;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class SaveStateController {
    MapRoomNode mapNode;
    ArrayList<AbstractMonster> monsters;
    ArrayList<MonsterLoader> monsterData;
    ArrayList<ArrayList<MapRoomNodeLoader>> roomNodeLoaders;
    ListLoader listLoader;

    PlayerLoader playerLoader;
    MapRoomNodeLoader roomLoader;
    RngLoader rngLoader;
    CombatRewardScreenLoader combatRewardScreenLoader;
    AbstractDungeon.CurrentScreen screen;
    AbstractDungeon dungeon;

    public void saveState() {
        playerLoader = new PlayerLoader(AbstractDungeon.player);
        roomNodeLoaders = AbstractDungeon.map.stream()
                                             .map(list -> list.stream().map(MapRoomNodeLoader::new)
                                                              .collect(Collectors
                                                                      .toCollection(ArrayList::new)))
                                             .collect(Collectors.toCollection(ArrayList::new));

        mapNode = AbstractDungeon.currMapNode;
        monsters = (ArrayList<AbstractMonster>) mapNode.room.monsters.monsters.clone();
        screen = AbstractDungeon.screen;
        monsterData = mapNode.room.monsters.monsters.stream()
                                                    .map(monster -> new MonsterLoader(monster))
                                                    .collect(Collectors
                                                            .toCollection(ArrayList::new));
        roomLoader = new MapRoomNodeLoader(mapNode);
        combatRewardScreenLoader = new CombatRewardScreenLoader(AbstractDungeon.combatRewardScreen);
        rngLoader = new RngLoader();
        listLoader = new ListLoader();
    }

    public void loadState() {
        rngLoader.loadRng();
        AbstractDungeon.player = playerLoader.loadPlayer();

        AbstractDungeon.currMapNode = mapNode;
        AbstractDungeon.screen = screen;

        AbstractDungeon.isScreenUp = false;
        listLoader.loadLists();
//        mapNode = roomLoader.loadMapRoomNode();

        System.out.println(mapNode.room.monsters.monsters.size());

        System.out.println(mapNode.room.isBattleOver);
        System.out.println(AbstractDungeon.getMonsters().areMonstersDead());

        mapNode.room.monsters.update();
        mapNode.room.monsters.updateAnimations();
        mapNode.room.update();

        AbstractDungeon.dungeonMapScreen.close();

        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.NONE;

//        AbstractDungeon.combatRewardScreen
        System.out.println(mapNode.room.isBattleOver);


        AbstractDungeon.map = roomNodeLoaders.stream()
                                             .map(list -> list.stream()
                                                              .map(MapRoomNodeLoader::loadMapRoomNode)
                                                              .collect(Collectors
                                                                      .toCollection(ArrayList::new)))
                                             .collect(Collectors.toCollection(ArrayList::new));

        AbstractDungeon.combatRewardScreen = combatRewardScreenLoader.loadCombatRewardScreen();
    }
}
