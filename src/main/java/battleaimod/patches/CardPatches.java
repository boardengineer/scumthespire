package battleaimod.patches;

import basemod.ReflectionHacks;
import battleaimod.BattleAiMod;
import battleaimod.savestate.CardState;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.EscapeAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.PotionSlot;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToDiscardEffect;

import java.util.Iterator;
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
            clz = EscapeAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FastEscapePatch {
        public static SpireReturn Prefix(EscapeAction _instance) {
            if (shouldGoFast()) {
                AbstractMonster m = (AbstractMonster) _instance.source;
                m.escape();
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractCreature.class,
            paramtypez = {},
            method = "updateAnimations"
    )
    public static class NoUpdateCreatureAnimationsPlayerPatch {
        public static SpireReturn Prefix(AbstractCreature _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
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

                return SpireReturn.Return(card);
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
            clz = AbstractPlayer.class,
            paramtypez = {AbstractPotion.class},
            method = "obtainPotion"
    )
    public static class QuietPotionsPatch {
        public static SpireReturn Prefix(AbstractPlayer _instance, AbstractPotion potionToObtain) {
            if (shouldGoFast()) {
                int index = 0;

                for (Iterator var3 = _instance.potions.iterator(); var3.hasNext(); ++index) {
                    AbstractPotion p = (AbstractPotion) var3.next();
                    if (p instanceof PotionSlot) {
                        break;
                    }
                }

                if (index < _instance.potionSlots) {
                    _instance.potions.set(index, potionToObtain);
                    potionToObtain.setAsObtained(index);
                    return SpireReturn.Return(true);
                } else {
                    AbstractDungeon.topPanel.flashRed();
                    return SpireReturn.Return(false);
                }
            }
            return SpireReturn.Continue();
        }
    }
}
