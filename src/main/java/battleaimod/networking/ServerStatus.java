package battleaimod.networking;

import battleaimod.BattleAiMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import ludicrousspeed.LudicrousSpeedMod;
import ludicrousspeed.simulator.patches.ServerStartupPatches;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerStatus {
    private static final int SERVER_GAME_PORT = 5124;

    @SpirePatch(clz = ServerStartupPatches.GameStartupPatch.class, method = "afterStart")
    public static class StartStatusServer {
        @SpirePostfixPatch
        public static void afterAfterStart(CardCrawlGame game) {
            if (LudicrousSpeedMod.plaidMode) {
                startServerStatusThread();
            }
        }
    }

    private static void startServerStatusThread() {
        new Thread(() -> {
            try {
                Thread.sleep(5_000);

                System.out.println("Starting status thread");
                ServerSocket serverGameServerSocket = new ServerSocket(SERVER_GAME_PORT);
                Socket serverGameSocket = serverGameServerSocket.accept();

                while (true) {
                    DataInputStream serverInputStream = new DataInputStream(new BufferedInputStream(serverGameSocket
                            .getInputStream()));
                    DataOutputStream serverOutputStream = new DataOutputStream(serverGameSocket
                            .getOutputStream());

                    System.out.println("Waiting for game to start...");

                    String clientRequest = serverInputStream.readUTF();

                    JsonObject response = new JsonObject();

                    boolean controllerRunning = BattleAiMod.battleAiController != null;

                    response.addProperty("yay", "yay?");
                    response.addProperty("ready", !controllerRunning);

                    serverOutputStream.writeUTF(response.toString());
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
