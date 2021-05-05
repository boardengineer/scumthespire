package battleaimod.fastobjects.actions;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;

import java.util.Iterator;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class DiscardCardActionFast extends AbstractGameAction {
    public static final String[] TEXT;
    private static final UIStrings uiStrings;
    private static final float DURATION;
    public static int numDiscarded;
    private boolean shouldSkipEverything = false;
    public boolean secondHalfOnly = false;
    private boolean forceNotDone = false;

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

    public DiscardCardActionFast(AbstractGameAction action) {
        this.p = (AbstractPlayer) action.target;
        this.isRandom = ReflectionHacks.getPrivate(action, DiscardAction.class, "isRandom");
        this.setValues(action.target, action.target, action.amount);
        this.actionType = AbstractGameAction.ActionType.DISCARD;
        this.endTurn = ReflectionHacks.getPrivate(action, DiscardAction.class, "endTurn");
        this.duration = DURATION;
        this.amount = action.amount;
    }

    public void update() {
        isDone = true;

        if (shouldGoFast()) {
            this.duration = 0;
        } else {
            System.err.println("Discard action updating");
        }

        if (AbstractDungeon.getMonsters().areMonstersBasicallyDead() || shouldSkipEverything) {
            this.isDone = true;
            this.shouldSkipEverything = true;
            return;
        }

        int handSize;
        if (this.p.hand.size() <= this.amount) {

            shouldSkipEverything = true;

            this.amount = this.p.hand.size();
            handSize = this.p.hand.size();

            for (int i = 0; i < handSize; ++i) {
                AbstractCard card = this.p.hand.getTopCard();
                this.p.hand.moveToDiscardPile(card);
                if (!this.endTurn) {
                    card.triggerOnManualDiscard();
                }
            }

            AbstractDungeon.player.hand.applyPowers();
            duration = 0;
            isDone = true;
            return;
        }

        if (!secondHalfOnly) {
            secondHalfOnly = true;
            if (!this.isRandom) {
                if (this.amount < 0) {
                    AbstractDungeon.handCardSelectScreen.open(TEXT[0], 99, true, true);
                    forceNotDone = true;
                    AbstractDungeon.player.hand.applyPowers();
                    this.tickDuration();
                    if(forceNotDone) {
                        this.isDone = false;
                    }
                    return;
                }

                numDiscarded = this.amount;
                if (this.p.hand.size() > this.amount) {
                    AbstractDungeon.handCardSelectScreen.open(TEXT[0], this.amount, false);
                    forceNotDone = true;
                }

                AbstractDungeon.player.hand.applyPowers();
                this.tickDuration();
                if(forceNotDone) {
                    this.isDone = false;
                }
                return;
            }
    
            for(int i=0; i < this.amount; i++)
            {
                AbstractCard card = this.p.hand.getRandomCard(AbstractDungeon.cardRandomRng);
                this.p.hand.moveToDiscardPile(card);
                card.triggerOnManualDiscard();
                GameActionManager.incrementDiscard(this.endTurn);
            }
        }

        if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved) {
            forceNotDone = false;
    
            for(final AbstractCard card : AbstractDungeon.handCardSelectScreen.selectedCards.group) {
                this.p.hand.moveToDiscardPile(card);
                card.triggerOnManualDiscard();
        
                if(!shouldGoFast()) {
                    System.err.println("Incrementing Discard");
                }
                GameActionManager.incrementDiscard(this.endTurn);
            }

            AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = true;
        }

        this.tickDuration();
        if(forceNotDone) {
            this.isDone = false;
        }
    }
}

