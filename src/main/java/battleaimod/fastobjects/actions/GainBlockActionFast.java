package battleaimod.fastobjects.actions;

import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.Iterator;

public class GainBlockActionFast extends GainBlockAction {
    private static final float DUR = 0.25F;

    public GainBlockActionFast(AbstractCreature target, int amount) {
        super(target, amount);
    }

    public GainBlockActionFast(AbstractCreature target, AbstractCreature source, int amount) {
        super(target, source, amount);
    }

    public GainBlockActionFast(AbstractCreature target, int amount, boolean superFast) {
        super(target, amount, superFast);
    }

    public GainBlockActionFast(AbstractCreature target, AbstractCreature source, int amount, boolean superFast) {
        super(target, source, amount, superFast);
    }

    public void update() {
        if (!this.target.isDying && !this.target.isDead && this.duration == this.startDuration) {
            this.target.addBlock(this.amount);
            Iterator var1 = AbstractDungeon.player.hand.group.iterator();

            while (var1.hasNext()) {
                AbstractCard c = (AbstractCard) var1.next();
                c.applyPowers();
            }
        }

        this.tickDuration();
    }
}
