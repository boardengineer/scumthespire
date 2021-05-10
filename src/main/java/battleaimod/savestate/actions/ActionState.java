package battleaimod.savestate.actions;

import battleaimod.BattleAiMod;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.ShoutAction;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.TextAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public interface ActionState {
    AbstractGameAction loadAction();

    static int indexForCreature(AbstractCreature creature) {
        if (creature.isPlayer) {
            return -1;
        } else {
            int foundIndex = -1;
            for (int i = 0; i < AbstractDungeon.getMonsters().monsters.size(); i++) {
                if (AbstractDungeon.getMonsters().monsters.get(i) == creature) {
                    foundIndex = i;
                    break;
                }
            }

            if (foundIndex == -1) {
                throw new IllegalStateException("No Target for " + creature + " " + AbstractDungeon
                        .getMonsters().monsters);
            } else {
                return foundIndex;
            }
        }
    }

    static AbstractCreature creatureForIndex(int index) {
        return index == -1 ? AbstractDungeon.player : AbstractDungeon
                .getMonsters().monsters.get(index);
    }

    Set<Class<? extends AbstractGameAction>> IGNORED_ACTIONS = new HashSet<Class<? extends AbstractGameAction>>() {{
        add(VFXAction.class);
        add(ShoutAction.class);
        add(TalkAction.class);
        add(TextAboveCreatureAction.class);
        add(SFXAction.class);
        add(RelicAboveCreatureAction.class);
        add(WaitAction.class);
    }};

    static ArrayList<ActionState> getActionQueueState() {
        ArrayList<ActionState> actionQueue = new ArrayList<>();

        for (AbstractGameAction action : AbstractDungeon.actionManager.actions) {
            if (BattleAiMod.actionByClassMap.containsKey(action.getClass())) {
                actionQueue.add(BattleAiMod.actionByClassMap.get(action.getClass()).factory
                        .apply(action));
            } else if (ActionState.IGNORED_ACTIONS.contains(action.getClass())) {
                // These are visual effects that are not worth encoding
            } else {
                throw new IllegalArgumentException("Unknown action type found in action manager: " + action);
            }
        }

        return actionQueue;
    }
}
