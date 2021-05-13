package battleaimod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class PlayerPatches {
    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {},
            method = "draw"
    )
    public static class NoSoundDrawPatch {
        public static void Replace(AbstractPlayer _instance) {
            if (_instance.hand.size() == 10) {
                _instance.createHandIsFullDialog();
            } else {
                _instance.draw(1);
                _instance.onCardDrawOrDiscard();
            }
        }
    }

    // THIS IS VOODOO, DON'T TOUCH IT
    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {int.class},
            method = "draw"
    )
    public static class NoSoundDrawPatch2 {
        @SpirePrefixPatch
        public static SpireReturn fastDraw(AbstractPlayer player, int numCards) {
            if (shouldGoFast()) {
                for (int i = 0; i < numCards; ++i) {
                    if (!player.drawPile.isEmpty()) {
                        AbstractCard card = player.drawPile.getTopCard();

                        card.triggerWhenDrawn();
                        player.hand.addToHand(card);
                        player.drawPile.removeTopCard();

                        player.powers.forEach(power -> power.onCardDraw(card));
                        player.relics.forEach(relic -> relic.onCardDraw(card));
                    }
                }
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "combatUpdate")
    public static class NoCombatUpdatePatch {
        @SpirePrefixPatch
        public static SpireReturn skipInFastMode(AbstractPlayer player) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "updateInput")
    public static class FastUpdateInputPatch {
        @SpirePrefixPatch
        public static SpireReturn skipInFastMode(AbstractPlayer player) {
            if (shouldGoFast()) {
                if (!player.endTurnQueued) {
                    if (!AbstractDungeon.actionManager.turnHasEnded) {
                        return SpireReturn.Return(null);
                    }
                } else if (AbstractDungeon.actionManager.cardQueue
                        .isEmpty() && !AbstractDungeon.actionManager.hasControl) {
                    player.endTurnQueued = false;
                    player.isEndingTurn = true;
                }

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = GainBlockAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class GainBlockActionFastPatch {
        @SpirePostfixPatch
        public static void insantUpdatePatch(GainBlockAction action) {
            if (shouldGoFast()) {
                action.isDone = true;
            }
        }
    }
}
