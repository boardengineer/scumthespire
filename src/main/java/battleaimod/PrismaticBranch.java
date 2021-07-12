package battleaimod;

import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;
import java.util.Iterator;

public class PrismaticBranch extends CustomRelic {
    private static final Texture IMAGE = new Texture("img/PrismaticBranch.png");

    public static CardGroup commonCardPool;
    public static CardGroup uncommonCardPool;
    public static CardGroup rareCardPool;

    public static CardGroup srcCommonCardPool;
    public static CardGroup srcUncommonCardPool;
    public static CardGroup srcRareCardPool;

    public static ArrayList<AbstractCard> completeList = new ArrayList();

    public static final String ID = "PrismaticBranch";

    public PrismaticBranch() {
        super(ID, IMAGE, RelicTier.RARE, LandingSound.FLAT);
    }

    public void onExhaust(AbstractCard card) {
        if (!AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
            this.flash();
            this.addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
            this.addToBot(new MakeTempCardInHandAction(returnRandomCard().makeCopy(), false));
        }
    }

    public String getUpdatedDescription() {
        return "The Branch of infinite memes designed for the TSS stream.";
    }

    public AbstractRelic makeCopy() {
        return new PrismaticBranch();
    }

    public static AbstractCard returnRandomCard() {
        return completeList.get(AbstractDungeon.cardRandomRng.random(completeList.size() - 1));
    }

    public static void initializeCardPools() {
        commonCardPool = new CardGroup(CardGroup.CardGroupType.CARD_POOL);
        uncommonCardPool = new CardGroup(CardGroup.CardGroupType.CARD_POOL);
        rareCardPool = new CardGroup(CardGroup.CardGroupType.CARD_POOL);

        commonCardPool.clear();
        uncommonCardPool.clear();
        rareCardPool.clear();
        ArrayList<AbstractCard> tmpPool = new ArrayList();

        CardLibrary.addRedCards(tmpPool);
        CardLibrary.addGreenCards(tmpPool);
        CardLibrary.addBlueCards(tmpPool);
        CardLibrary.addPurpleCards(tmpPool);

        Iterator var4 = tmpPool.iterator();

        AbstractCard c;
        while (var4.hasNext()) {
            c = (AbstractCard) var4.next();
            if (!CardLibrary.cards.containsKey(c.cardID)) {
                continue;
            }

            switch (c.rarity) {
                case COMMON:
                    commonCardPool.addToTop(c);
                    break;
                case UNCOMMON:
                    uncommonCardPool.addToTop(c);
                    break;
                case RARE:
                    rareCardPool.addToTop(c);
                    break;
                case CURSE:
                    break;
            }
        }

        srcRareCardPool = new CardGroup(CardGroup.CardGroupType.CARD_POOL);
        srcUncommonCardPool = new CardGroup(CardGroup.CardGroupType.CARD_POOL);
        srcCommonCardPool = new CardGroup(CardGroup.CardGroupType.CARD_POOL);

        var4 = rareCardPool.group.iterator();

        while (var4.hasNext()) {
            c = (AbstractCard) var4.next();
            srcRareCardPool.addToBottom(c);
        }

        var4 = uncommonCardPool.group.iterator();

        while (var4.hasNext()) {
            c = (AbstractCard) var4.next();
            srcUncommonCardPool.addToBottom(c);
        }

        var4 = commonCardPool.group.iterator();

        while (var4.hasNext()) {
            c = (AbstractCard) var4.next();
            srcCommonCardPool.addToBottom(c);
        }
        //        AbstractCard.CardRarity rarity = AbstractCard.CardRarity.RARE;
//        if (rarity.equals(AbstractCard.CardRarity.COMMON)) {
        completeList.addAll(srcCommonCardPool.group);
//        } else if (rarity.equals(AbstractCard.CardRarity.UNCOMMON)) {
        completeList.addAll(srcUncommonCardPool.group);
//        } else {
        completeList.addAll(srcRareCardPool.group);
//        }
    }
}
