package communicationmod.fastobjects.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.TipTracker;
import com.megacrit.cardcrawl.localization.TutorialStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.ui.FtueTip;

import java.util.Iterator;

public class EmptyDeckShuffleActionFast extends AbstractGameAction {
    public static final String[] MSG;
    public static final String[] LABEL;
    private static final TutorialStrings tutorialStrings;

    static {
        tutorialStrings = CardCrawlGame.languagePack.getTutorialString("Shuffle Tip");
        MSG = tutorialStrings.TEXT;
        LABEL = tutorialStrings.LABEL;
    }

    boolean didTheThing = false;
    private boolean shuffled = false;
    private boolean vfxDone = false;
    private int count = 0;

    public EmptyDeckShuffleActionFast() {
        this.setValues(null, null, 0);
        this.actionType = AbstractGameAction.ActionType.SHUFFLE;
        if (!(Boolean) TipTracker.tips.get("SHUFFLE_TIP")) {
            AbstractDungeon.ftue = new FtueTip(LABEL[0], MSG[0], (float) Settings.WIDTH / 2.0F, (float) Settings.HEIGHT / 2.0F, FtueTip.TipType.SHUFFLE);
            TipTracker.neverShowAgain("SHUFFLE_TIP");
        }

        Iterator var1 = AbstractDungeon.player.relics.iterator();

        while (var1.hasNext()) {
            AbstractRelic r = (AbstractRelic) var1.next();
            r.onShuffle();
        }

    }

    public void update() {
        if (didTheThing) {
            return;
        }

        didTheThing = true;
        if (!this.shuffled) {
            this.shuffled = true;
            AbstractDungeon.player.discardPile.shuffle(AbstractDungeon.shuffleRng);
        }

        if (!this.vfxDone) {
            Iterator<AbstractCard> c = AbstractDungeon.player.discardPile.group.iterator();

            while (c.hasNext()) {
                ++this.count;
                AbstractCard e = c.next();
                c.remove();
                AbstractDungeon.player.drawPile.addToBottom(e);
            }
            this.vfxDone = true;
        }

        this.isDone = true;
    }
}
