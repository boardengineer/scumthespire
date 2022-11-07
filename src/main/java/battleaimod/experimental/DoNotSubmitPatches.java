package battleaimod.experimental;

import battleaimod.BattleAiMod;
import battleaimod.networking.AiClient;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.GameCursor;
import com.megacrit.cardcrawl.helpers.input.InputActionSet;

import java.io.IOException;

public class DoNotSubmitPatches {
    public static boolean hideCursor = false;

    @SpirePatch(clz = CardCrawlGame.class, method = "update")
    public static class AlwaysDebugButtonsPatch {
        @SpirePostfixPatch
        public static void Debug(CardCrawlGame cardCrawlGame) {
            if (InputActionSet.down.isJustPressed()) {
                //hideCursor = !hideCursor;
            }

            if (InputActionSet.up.isJustPressed()) {
                //startClient();
            }
        }
    }

    @SpirePatch(clz = GameCursor.class, method = "render")
    public static class MaybeHideCursorPatch {
        @SpirePrefixPatch
        public static void maybeHideCursor() {
            GameCursor.hidden = hideCursor;
        }
    }

    public static void startClient() {
        if (BattleAiMod.aiClient == null) {
            try {
                BattleAiMod.aiClient = new AiClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (BattleAiMod.aiClient != null) {
            BattleAiMod.aiClient.sendState();
        }
    }
}
