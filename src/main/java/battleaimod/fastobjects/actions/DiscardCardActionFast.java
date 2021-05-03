package battleaimod.fastobjects.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;

import java.util.Iterator;

public class DiscardCardActionFast extends AbstractGameAction {
    public static final String[] TEXT;
    private static final UIStrings uiStrings;
    private static final float DURATION;
    public static int numDiscarded;
    private boolean shouldSkipEverything = false;
    public boolean secondHalfOnly = false;

    static {
        uiStrings = CardCrawlGame.languagePack.getUIString("DiscardAction");
        TEXT = uiStrings.TEXT;
        DURATION = Settings.ACTION_DUR_XFAST;
    }

    private final AbstractPlayer p;
    private final boolean isRandom;
    private final boolean endTurn;

    public DiscardCardActionFast(AbstractCreature target, AbstractCreature source, int amount, boolean isRandom) {
        this(target, source, amount, isRandom, false);
    }

    public DiscardCardActionFast(AbstractCreature target, AbstractCreature source, int amount, boolean isRandom, boolean endTurn) {
        this.p = (AbstractPlayer) target;
        this.isRandom = isRandom;
        this.setValues(target, source, amount);
        this.actionType = AbstractGameAction.ActionType.DISCARD;
        this.endTurn = endTurn;
        this.duration = DURATION;
    }

    public void update()
    {
        isDone = true;
        this.duration = 0;
        if(!secondHalfOnly)
        {
            AbstractCard c;
        
            if(AbstractDungeon.getMonsters().areMonstersBasicallyDead() || shouldSkipEverything)
            {
                this.isDone = true;
                this.shouldSkipEverything = true;
                return;
            }
        
            int handSize;
            if(this.p.hand.size()<=this.amount)
            {
        
                shouldSkipEverything = true;
        
                this.amount = this.p.hand.size();
                handSize = this.p.hand.size();
        
                for(int i = 0; i<handSize; ++i)
                {
                    AbstractCard card = this.p.hand.getTopCard();
                    this.p.hand.moveToDiscardPile(card);
                    if(!this.endTurn)
                    {
                        card.triggerOnManualDiscard();
                    }
            
                    GameActionManager.incrementDiscard(this.endTurn);
                }
        
                AbstractDungeon.player.hand.applyPowers();
                duration = 0;
                isDone = true;
                return;
            }
        
            if(!this.isRandom)
            {
                if(this.amount<0)
                {
                    AbstractDungeon.handCardSelectScreen.open(TEXT[0], 99, true, true);
                    AbstractDungeon.player.hand.applyPowers();
                    this.tickDuration();
                    return;
                }
            
                numDiscarded = this.amount;
                if(this.p.hand.size()>this.amount)
                {
                    AbstractDungeon.handCardSelectScreen.open(TEXT[0], this.amount, false);
                }
            
                AbstractDungeon.player.hand.applyPowers();
                this.tickDuration();
                return;
            }
        
            for(int i = 0; i<this.amount; ++i)
            {
                c = this.p.hand.getRandomCard(AbstractDungeon.cardRandomRng);
                this.p.hand.moveToDiscardPile(c);
                c.triggerOnManualDiscard();
                GameActionManager.incrementDiscard(this.endTurn);
            }
        }

        if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved) {
    
            for(final AbstractCard c : AbstractDungeon.handCardSelectScreen.selectedCards.group)
            {
                this.p.hand.moveToDiscardPile(c);
                c.triggerOnManualDiscard();
                GameActionManager.incrementDiscard(this.endTurn);
            }

            AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = true;
        }

        this.tickDuration();
    }
}

