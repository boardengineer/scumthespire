//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package battleaimod.fastobjects.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.HandCheckAction;
import com.megacrit.cardcrawl.actions.utility.ShowCardAction;
import com.megacrit.cardcrawl.actions.utility.ShowCardAndPoofAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.AbstractCard.CardType;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

// The normal UserCardAction does a bunch of triggering during the constructor, this dupe
// bypasses the triggering constructor logic and only performs the action
public class UpdateOnlyUseCardAction extends AbstractGameAction {
    private final AbstractCard targetCard;
    public AbstractCreature target;
    public boolean exhaustCard;
    public boolean reboundCard;

    public UpdateOnlyUseCardAction(AbstractCard card, AbstractCreature target) {
        this.target = null;
        this.reboundCard = false;
        this.targetCard = card;
        this.target = target;
        if (card.exhaustOnUseOnce || card.exhaust) {
            this.exhaustCard = true;
        }

        this.setValues(AbstractDungeon.player, null, 1);
        this.duration = 0.15F;
        if (this.exhaustCard) {
            this.actionType = ActionType.EXHAUST;
        } else {
            this.actionType = ActionType.USE;
        }
    }

    public UpdateOnlyUseCardAction(AbstractCard targetCard) {
        this(targetCard, null);
    }

    public void update() {
        if (this.duration == 0.15F) {
            this.targetCard.freeToPlayOnce = false;
            this.targetCard.isInAutoplay = false;
            if (this.targetCard.purgeOnUse) {
                this.addToTop(new ShowCardAndPoofAction(this.targetCard));
                this.isDone = true;
                AbstractDungeon.player.cardInUse = null;
                return;
            }

            if (this.targetCard.type == CardType.POWER) {
                this.addToTop(new ShowCardAction(this.targetCard));
                if (Settings.FAST_MODE) {
                    this.addToTop(new WaitAction(0.1F));
                } else {
                    this.addToTop(new WaitAction(0.7F));
                }

                AbstractDungeon.player.hand.empower(this.targetCard);
                this.isDone = true;
                AbstractDungeon.player.hand.applyPowers();
                AbstractDungeon.player.hand.glowCheck();
                AbstractDungeon.player.cardInUse = null;
                return;
            }

            AbstractDungeon.player.cardInUse = null;
            boolean spoonProc = false;
            if (this.exhaustCard && AbstractDungeon.player
                    .hasRelic("Strange Spoon") && this.targetCard.type != CardType.POWER) {
                spoonProc = AbstractDungeon.cardRandomRng.randomBoolean();
            }

            if (this.exhaustCard && !spoonProc) {
                AbstractDungeon.player.hand.moveToExhaustPile(this.targetCard);
                CardCrawlGame.dungeon.checkForPactAchievement();
            } else {
                if (spoonProc) {
                    AbstractDungeon.player.getRelic("Strange Spoon").flash();
                }

                if (this.reboundCard) {
                    AbstractDungeon.player.hand.moveToDeck(this.targetCard, false);
                } else if (this.targetCard.shuffleBackIntoDrawPile) {
                    AbstractDungeon.player.hand.moveToDeck(this.targetCard, true);
                } else if (this.targetCard.returnToHand) {
                    AbstractDungeon.player.hand.moveToHand(this.targetCard);
                    AbstractDungeon.player.onCardDrawOrDiscard();
                } else {
                    AbstractDungeon.player.hand.moveToDiscardPile(this.targetCard);
                }
            }

            this.targetCard.exhaustOnUseOnce = false;
            this.targetCard.dontTriggerOnUseCard = false;
            this.addToBot(new HandCheckAction());
        }

        this.tickDuration();
    }
}
