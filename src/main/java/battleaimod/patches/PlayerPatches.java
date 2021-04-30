package battleaimod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;

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
        private static final Logger logger = LogManager.getLogger(AbstractPlayer.class.getName());

        public static void Replace(AbstractPlayer _instance, int numCards) {
            for (int i = 0; i < numCards; ++i) {
                if (_instance.drawPile.isEmpty()) {
                    logger.info("ERROR: How did this happen? No cards in draw pile?? Player.java");
                } else {
                    AbstractCard c = _instance.drawPile.getTopCard();
                    c.current_x = CardGroup.DRAW_PILE_X;
                    c.current_y = CardGroup.DRAW_PILE_Y;
                    c.setAngle(0.0F, true);
                    c.lighten(false);
                    c.drawScale = 0.12F;
                    c.targetDrawScale = 0.75F;
                    c.triggerWhenDrawn();
                    _instance.hand.addToHand(c);
                    _instance.drawPile.removeTopCard();
                    Iterator var4 = _instance.powers.iterator();

                    while (var4.hasNext()) {
                        AbstractPower p = (AbstractPower) var4.next();
                        p.onCardDraw(c);
                    }

                    var4 = _instance.relics.iterator();

                    while (var4.hasNext()) {
                        AbstractRelic r = (AbstractRelic) var4.next();
                        r.onCardDraw(c);
                    }
                }
            }
        }
    }
}
