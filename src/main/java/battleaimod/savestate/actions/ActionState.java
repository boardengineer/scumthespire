package battleaimod.savestate.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

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
}
