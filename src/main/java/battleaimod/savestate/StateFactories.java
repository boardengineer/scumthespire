
package battleaimod.savestate;

import battleaimod.savestate.actions.Action;
import battleaimod.savestate.actions.CurrentAction;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.orbs.Orb;
import battleaimod.savestate.powers.Power;
import battleaimod.savestate.relics.Relic;

import java.util.HashMap;

/**
 * This class contains maps to state factories.  Modded content can be included in the state saver
 * by adding keys and factories directly to these maps
 */
public class StateFactories {
    public static HashMap<String, Monster> monsterByIdMap = createMonsterMap();
    public static HashMap<String, Power> powerByIdMap = createPowerMap();
    public static HashMap<String, Relic> relicByIdMap = createRelicMap();
    public static HashMap<Class, Action> actionByClassMap = createActionMap();
    public static HashMap<Class, CurrentAction> currentActionByClassMap = createCurrentActionMap();
    public static HashMap<Class, Orb> orbByClassMap = createOrbMap();

    private static HashMap<String, Monster> createMonsterMap() {
        HashMap<String, Monster> monsterByIdmap = new HashMap<>();
        for (Monster monster : Monster.values()) {
            monsterByIdmap.put(monster.monsterId, monster);
        }
        return monsterByIdmap;
    }

    private static HashMap<String, Power> createPowerMap() {
        HashMap<String, Power> powerByIdmap = new HashMap<>();
        for (Power power : Power.values()) {
            powerByIdmap.put(power.powerId, power);
        }
        return powerByIdmap;
    }

    private static HashMap<String, Relic> createRelicMap() {
        HashMap<String, Relic> relicByIdMap = new HashMap<>();
        for (Relic relic : Relic.values()) {
            relicByIdMap.put(relic.relicId, relic);
        }
        return relicByIdMap;
    }

    private static HashMap<Class, Action> createActionMap() {
        HashMap<Class, Action> actionByClassMap = new HashMap<>();
        for (Action action : Action.values()) {
            actionByClassMap.put(action.actionClass, action);
        }
        return actionByClassMap;
    }

    private static HashMap<Class, CurrentAction> createCurrentActionMap() {
        HashMap<Class, CurrentAction> currentActionByClassMap = new HashMap<>();
        for (CurrentAction action : CurrentAction.values()) {
            currentActionByClassMap.put(action.actionClass, action);
        }
        return currentActionByClassMap;
    }

    private static HashMap<Class, Orb> createOrbMap() {
        HashMap<Class, Orb> orbByClassMap = new HashMap<>();
        for (Orb orb : Orb.values()) {
            orbByClassMap.put(orb.orbClass, orb);
        }
        return orbByClassMap;
    }
}
