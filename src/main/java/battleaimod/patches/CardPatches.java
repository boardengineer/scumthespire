package battleaimod.patches;

import basemod.ReflectionHacks;
import battleaimod.BattleAiMod;
import battleaimod.fastobjects.actions.EmptyDeckShuffleActionFast;
import battleaimod.savestate.CardState;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.common.PlayTopCardAction;
import com.megacrit.cardcrawl.actions.utility.ShowCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.green.DaggerSpray;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToDiscardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToHandEffect;

import java.util.UUID;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class CardPatches {
    // Turn off Image loading from the constructor, it's slow.
    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {String.class, String.class, String.class, int.class, String.class, AbstractCard.CardType.class, AbstractCard.CardColor.class, AbstractCard.CardRarity.class, AbstractCard.CardTarget.class, DamageInfo.DamageType.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class FastCardConstructorPatch {
        @SpireInsertPatch(loc = 391)
        public static SpireReturn Insert(AbstractCard _instance, String id, String name, String imgUrl, int cost, String rawDescription, AbstractCard.CardType type, AbstractCard.CardColor color, AbstractCard.CardRarity rarity, AbstractCard.CardTarget target, DamageInfo.DamageType dType) {
            if (shouldGoFast()) {
                _instance.originalName = name;
                _instance.name = name;
                _instance.cardID = id;
                _instance.assetUrl = imgUrl;
                _instance.cost = cost;
                _instance.costForTurn = cost;
                _instance.rawDescription = rawDescription;
                _instance.type = type;
                _instance.color = color;
                _instance.rarity = rarity;
                _instance.target = target;

                ReflectionHacks.setPrivate(_instance, AbstractCard.class, "damageType", dType);
                _instance.damageTypeForTurn = dType;
                _instance.uuid = UUID.randomUUID();

                return SpireReturn.Return(null);

            }
            return SpireReturn.Continue();
        }
    }

    // Fast Mode doesn't load images which will NPE when trying to render, turn off rendering
    // in fast mode.
    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {SpriteBatch.class, boolean.class, boolean.class},
            method = "renderCard"
    )
    public static class NoRenderCardsPatch {
        public static SpireReturn Prefix(AbstractCard _instance, SpriteBatch sb, boolean hovered, boolean selected) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {},
            method = "updateTransparency"
    )
    public static class NoUpdateTransparencyPatch {
        public static SpireReturn Prefix(AbstractCard _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = CardGroup.class,
            paramtypez = {},
            method = "glowCheck"
    )
    public static class NoGlowCheckPatch {
        public static SpireReturn Prefix(CardGroup _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = CardGroup.class,
            paramtypez = {AbstractCard.class},
            method = "moveToDiscardPile"
    )
    public static class FastDiscardPatch {
        public static SpireReturn Prefix(CardGroup _instance, AbstractCard card) {
            int startingSize = _instance.group.size();

            ReflectionHacks
                    .privateMethod(CardGroup.class, "resetCardBeforeMoving", AbstractCard.class)
                    .invoke(_instance, card);

            if (_instance.group.size() == startingSize) {
                for (AbstractCard groupCard : _instance.group) {
                    if (groupCard.uuid.equals(card.uuid)) {
                        _instance.group.remove(groupCard);
                        break;
                    }
                }
            }

            AbstractDungeon.player.discardPile.addToTop(card);

            AbstractDungeon.player.onCardDrawOrDiscard();
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoUpdatePlayerPatch {
        public static SpireReturn Prefix(AbstractPlayer _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = GameActionManager.class,
            paramtypez = {},
            method = "update"
    )
    public static class MaybeSkipActionUpdatePatch {
        public static SpireReturn Prefix(GameActionManager _instance) {
            if (shouldGoFast()) {
//                if (AbstractDungeon.isScreenUp) {
//                    return SpireReturn.Return(null);
//                }
            }
            return SpireReturn.Continue();
        }
    }


    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {},
            method = "updateColor"
    )
    public static class NoUpdateColorPatch {
        public static SpireReturn Prefix(AbstractCard _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {},
            method = "unfadeOut"
    )
    public static class NoFadeOutPatch {
        public static SpireReturn Prefix(AbstractCard _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {boolean.class},
            method = "darken"
    )
    public static class NoDarkenPatch {
        public static SpireReturn Prefix(AbstractCard _instance, boolean immediate) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {},
            method = "makeStatEquivalentCopy"
    )
    public static class UseCardPoolForRandomCreationPatch {
        public static SpireReturn Prefix(AbstractCard _instance) {
            if (shouldGoFast()) {
                long makeCardCopyStart = System.currentTimeMillis();

                AbstractCard card = CardState.getCard(_instance.cardID);
                for (int i = 0; i < _instance.timesUpgraded; ++i) {
                    card.upgrade();
                }

                card.name = _instance.name;
                card.target = _instance.target;
                card.upgraded = _instance.upgraded;
                card.timesUpgraded = _instance.timesUpgraded;
                card.baseDamage = _instance.baseDamage;
                card.baseBlock = _instance.baseBlock;
                card.baseMagicNumber = _instance.baseMagicNumber;
                card.cost = _instance.cost;
                card.costForTurn = _instance.costForTurn;
                card.isCostModified = _instance.isCostModified;
                card.isCostModifiedForTurn = _instance.isCostModifiedForTurn;
                card.inBottleLightning = _instance.inBottleLightning;
                card.inBottleFlame = _instance.inBottleFlame;
                card.inBottleTornado = _instance.inBottleTornado;
                card.isSeen = _instance.isSeen;
                card.isLocked = _instance.isLocked;
                card.misc = _instance.misc;
                card.freeToPlayOnce = _instance.freeToPlayOnce;

                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController.addRuntime("makeStatEquivalentCopy", System
                            .currentTimeMillis() - makeCardCopyStart);
                }

//                System.out.println("Here i am!!!! " + card.cardID);

                return SpireReturn.Return(card);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            paramtypez = {},
            method = "makeSameInstanceOf"
    )
    public static class UseCardPoolForRandomCreationAndInstancePatch {
        public static SpireReturn Prefix(AbstractCard _instance) {
            if (shouldGoFast()) {
                AbstractCard result = _instance.makeStatEquivalentCopy();

                while (result == null) {
                    System.err.println("Failed to create card, trying again...");
                    result = _instance.makeStatEquivalentCopy();
                }

                result.uuid = _instance.uuid;

                return SpireReturn.Return(result);
            }
            return SpireReturn.Continue();
        }
    }


    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {AbstractCard.class},
            method = "bottledCardUpgradeCheck"
    )
    public static class NoBottledDescriptionChangePatch {
        public static SpireReturn Prefix(AbstractPlayer _instance, AbstractCard card) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = ShowCardAndAddToDiscardEffect.class,
            paramtypez = {AbstractCard.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class ShowCardAndAddToDiscardEffectPatch {
        public static SpireReturn Prefix(ShowCardAndAddToDiscardEffect _instance, AbstractCard card) {
            if (shouldGoFast()) {

                if (card.type != AbstractCard.CardType.CURSE && card.type != AbstractCard.CardType.STATUS && AbstractDungeon.player
                        .hasPower("MasterRealityPower")) {
                    card.upgrade();
                }

                AbstractDungeon.player.discardPile.addToTop(card);

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = ShowCardAndAddToHandEffect.class,
            paramtypez = {AbstractCard.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class ShowCardAndAddToHandEffectPatch {
        public static SpireReturn Prefix(ShowCardAndAddToHandEffect _instance, AbstractCard card) {
            if (shouldGoFast()) {
                if (card == null) {
                    throw new IllegalStateException("Card Is null, Nothing to return ");
                }

                if (card.type != AbstractCard.CardType.CURSE && card.type != AbstractCard.CardType.STATUS && AbstractDungeon.player
                        .hasPower("MasterRealityPower")) {
                    card.upgrade();
                }

                AbstractDungeon.player.hand.addToTop(card);

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = MakeTempCardInHandAction.class,
            paramtypez = {},
            method = "makeNewCard"
    )
    public static class MakeNewCardPatch {
        public static SpireReturn Prefix(MakeTempCardInHandAction action) {
            if (shouldGoFast()) {
                AbstractCard card = ReflectionHacks
                        .getPrivate(action, MakeTempCardInHandAction.class, "c");
                boolean sameUUID = ReflectionHacks
                        .getPrivate(action, MakeTempCardInHandAction.class, "sameUUID");

                AbstractCard result = null;

                while (result == null) {
                    result = sameUUID ? card.makeSameInstanceOf() : card.makeStatEquivalentCopy();

                    if (result == null) {
                        System.err.println("Failed to create card, retrying");
                    }
                }


                return SpireReturn.Return(result);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = MakeTempCardInHandAction.class,
            paramtypez = {int.class},
            method = "addToHand"
    )
    public static class AddToHandPatch {
        public static SpireReturn Prefix(MakeTempCardInHandAction action, int handAmt) {
            if (shouldGoFast()) {
                for (int i = 0; i < handAmt; ++i) {
                    AbstractCard card = null;
                    while (card == null) {
                        card = (AbstractCard) MakeNewCardPatch.Prefix(action)
                                                              .get();
                        if (card == null) {
                            System.err.println("card was null, retrying...");
                        }
                    }

                    AbstractDungeon.player.hand.addToHand(card);
                    card.triggerWhenCopied();
                    AbstractDungeon.player.hand.applyPowers();
                    AbstractDungeon.player.onCardDrawOrDiscard();
                    if (AbstractDungeon.player
                            .hasPower("Corruption") && card.type == AbstractCard.CardType.SKILL) {
                        card.setCostForTurn(-9);
                    }
                }

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = MakeTempCardInDiscardAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class MakeTempCardsFastPatch {
        public static void Prefix(MakeTempCardInDiscardAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .00001F);

                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "startDuration", .00001F);
            }
        }

        public static void Postfix(MakeTempCardInDiscardAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
                CardState.freeCard(ReflectionHacks
                        .getPrivate(_instance, MakeTempCardInDiscardAction.class, "c"));
            }
        }
    }

    @SpirePatch(
            clz = PlayTopCardAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class PlayTopCardPatch {
        public static SpireReturn Prefix(PlayTopCardAction _instance) {
            if (shouldGoFast()) {
                if (AbstractDungeon.player.drawPile.isEmpty()) {
                    boolean exhaustCards = ReflectionHacks
                            .getPrivate(_instance, PlayTopCardAction.class, "exhaustCards");

                    AbstractDungeon.actionManager
                            .addToTop(new PlayTopCardAction(_instance.target, exhaustCards));
                    AbstractDungeon.actionManager.addToTop(new EmptyDeckShuffleActionFast());
                    _instance.isDone = true;
                    return SpireReturn.Return(null);
                }
            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = DaggerSpray.class,
            method = "use"
    )
    public static class NoFxDaggerSprayPatch {
        public static SpireReturn Prefix(DaggerSpray spray, AbstractPlayer p, AbstractMonster m) {
            if (shouldGoFast()) {

                AbstractDungeon.actionManager
                        .addToBottom(new DamageAllEnemiesAction(p, spray.multiDamage, spray.damageTypeForTurn, AbstractGameAction.AttackEffect.NONE));
                AbstractDungeon.actionManager
                        .addToBottom(new DamageAllEnemiesAction(p, spray.multiDamage, spray.damageTypeForTurn, AbstractGameAction.AttackEffect.NONE));

                return SpireReturn.Return(false);
            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = ShowCardAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FreeShownCardPatch {
        public static SpireReturn Prefix(ShowCardAction _instance) {
            if (shouldGoFast()) {
                AbstractCard card = ReflectionHacks
                        .getPrivate(_instance, ShowCardAction.class, "card");

                if (AbstractDungeon.player.limbo.contains(card)) {
                    AbstractDungeon.player.limbo.removeCard(card);
                    CardState.freeCard(card);
                }

                AbstractDungeon.player.cardInUse = null;
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }
}
