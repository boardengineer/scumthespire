package battleaimod.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;

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
}
