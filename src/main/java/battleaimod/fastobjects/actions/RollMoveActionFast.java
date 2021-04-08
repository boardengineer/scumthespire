//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package battleaimod.fastobjects.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class RollMoveActionFast extends AbstractGameAction {
    private final AbstractMonster monster;
    boolean rolled = false;

    public RollMoveActionFast(AbstractMonster monster) {
        this.monster = monster;
    }

    public void update() {
        if (rolled) {
            return;
        }
        rolled = true;
        this.monster.rollMove();
        this.isDone = true;
    }
}
