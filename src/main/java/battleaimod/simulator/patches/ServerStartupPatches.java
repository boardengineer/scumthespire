package battleaimod.simulator.patches;

import battleaimod.simulator.LudicrousSpeedMod;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.Exordium;
import com.megacrit.cardcrawl.rooms.EmptyRoom;

import java.util.ArrayList;

import static battleaimod.simulator.LudicrousSpeedMod.plaidMode;

public class ServerStartupPatches {
    @SpirePatch(clz = CardCrawlGame.class, method = "create")
    public static class GameStartupPatch {
        @SpirePostfixPatch
        public static void afterStart(CardCrawlGame game) {
            if (plaidMode) {
                System.err.println("Skipping Splash Screen for Char Select");

                // Sets the current dungeon
                Settings.seed = 123L;
                AbstractDungeon.generateSeeds();

                // TODO this needs to be the actual character class or bad things happen
                new Exordium(CardCrawlGame.characterManager
                        .getCharacter(AbstractPlayer.PlayerClass.DEFECT), new ArrayList<>());

                CardCrawlGame.dungeon.currMapNode.room = new EmptyRoom();

                CardCrawlGame.mode = CardCrawlGame.GameMode.GAMEPLAY;
            }
        }
    }

    @SpirePatch(clz = CardCrawlGame.class, method = "renderBlackFadeScreen")
    public static class SkipRenderBlackFadeScreen {
        @SpirePrefixPatch
        public static SpireReturn replaceOnServer(CardCrawlGame game, SpriteBatch sb) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "render")
    public static class NoRenderDungeon {
        @SpirePrefixPatch
        public static SpireReturn replaceOnServer(AbstractDungeon dungeon, SpriteBatch sb) {
            if (LudicrousSpeedMod.plaidMode) {
                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }
    }
}
