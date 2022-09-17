package battleaimod.networking;

import basemod.ClickableUIElement;
import battleaimod.BattleAiMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.steam.SteamSearch;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ExhaustCardEffect;
import ludicrousspeed.LudicrousSpeedMod;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URISyntaxException;

public class BattleClientController {
    private static final String HOST_IP = "127.0.0.1";

    private static final float X_POSITION = 10F * Settings.scale;
    private static final float Y_POSITION = (Settings.HEIGHT - Settings.HEIGHT / 5);

    private static final Texture START_BUTTON_TEXTURE = new Texture("img/StartSteve.png");
    private static final Texture BLUE_BUTTON_TEXTURE = new Texture("img/blueball.png");

    private static final float BANNER_WIDTH = (float) (START_BUTTON_TEXTURE.getWidth()) * 4f;

    StartUIButton startButton;
    BackendButton backendButton;

    private static final int SERVER_GAME_PORT = 5124;

    private static boolean serverReady = false;
    private static Process serverProcess;

    public BattleClientController() {
        startButton = new StartUIButton();
        backendButton = new BackendButton();
    }

    public void update() {
        if (!isEnabled()) {
            return;
        }

        startButton.update();
        backendButton.update();
    }

    public void render(SpriteBatch sb) {
        if (!LudicrousSpeedMod.plaidMode && isEnabled()) {
            if (inCombat()) {

                sb.setColor(Color.WHITE);

                // double the height for reasons?
                float height = (float) (START_BUTTON_TEXTURE.getHeight() * 2.2);
                float width = BANNER_WIDTH;

                float y = Y_POSITION - (height / 1.9f);
                float x = X_POSITION;

                sb.draw(ImageMaster.TOP_PANEL_BAR, x, y, width, height);

                startButton.render(sb);
                backendButton.render(sb);
            }
        }
    }

    public static class StartUIButton extends ClickableUIElement {
        public StartUIButton() {
            super(START_BUTTON_TEXTURE);
            x = 10F * Settings.scale;
            y = Y_POSITION;
            hitbox = new Hitbox(x, y, START_BUTTON_TEXTURE.getWidth(), START_BUTTON_TEXTURE
                    .getHeight());
        }

        @Override
        protected void onHover() {
            this.angle = MathHelper.angleLerpSnap(this.angle, 15.0F);
            this.tint.a = 0.25F;
        }

        @Override
        protected void onUnhover() {
            this.angle = MathHelper.angleLerpSnap(this.angle, 0.0F);
            this.tint.a = 0.0F;
        }

        @Override
        protected void onClick() {
            if (!canSendState()) {
                return;
            }

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

        @Override
        public void update() {
            super.update();
        }

        @Override
        public void render(SpriteBatch sb) {
            Color alpha = Color.WHITE.cpy();

            if (!canSendState()) {
                alpha = alpha.add(0, 0, 0, -.75f);
            }

            super.render(sb, alpha);
        }
    }

    public static class BackendButton extends ClickableUIElement {
        float width;
        float height;

        public BackendButton() {
            super(BLUE_BUTTON_TEXTURE);

            x = BANNER_WIDTH - START_BUTTON_TEXTURE.getWidth() * .75F;
            y = Y_POSITION;

            width = START_BUTTON_TEXTURE.getWidth();
            height = START_BUTTON_TEXTURE.getHeight();

            hitbox = new Hitbox(x, y, width, height);
        }

        @Override
        protected void onHover() {
            this.angle = MathHelper.angleLerpSnap(this.angle, 15.0F);
            this.tint.a = 0.25F;
        }

        @Override
        protected void onUnhover() {
            this.angle = MathHelper.angleLerpSnap(this.angle, 0.0F);
            this.tint.a = 0.0F;
        }

        @Override
        protected void onClick() {
            startServerThread();
        }

        @Override
        public void update() {
            super.update();
        }

        @Override
        public void render(SpriteBatch sb) {
            Color color;
            if (serverReady) {
                color = Color.WHITE;
            } else if (waitingForSeverStartup()) {
                color = Color.YELLOW;
            } else {
                color = Color.RED;
            }
            sb.setColor(color);
            float halfWidth;
            float halfHeight;
            if (this.image != null) {
                halfWidth = width / 2.0F;
                halfHeight = height / 2.0F;
                sb.draw(this.image, this.x - halfWidth + halfWidth * Settings.scale, this.y - halfHeight + halfHeight * Settings.scale, halfWidth, halfHeight, width, height, Settings.scale, Settings.scale, this.angle, 0, 0, this.image
                        .getWidth(), this.image.getHeight(), false, false);
                if (this.tint.a > 0.0F) {
                    sb.setBlendFunction(770, 1);
                    sb.setColor(this.tint);
                    sb.draw(this.image, this.x - halfWidth + halfWidth * Settings.scale, this.y - halfHeight + halfHeight * Settings.scale, halfWidth, halfHeight, width, height, Settings.scale, Settings.scale, this.angle, 0, 0, this.image
                            .getWidth(), this.image.getHeight(), false, false);
                    sb.setBlendFunction(770, 771);
                }
            }

            this.renderHitbox(sb);
        }
    }

    private static boolean inCombat() {
        return CardCrawlGame.isInARun() && AbstractDungeon.currMapNode != null && AbstractDungeon
                .getCurrRoom() != null && AbstractDungeon
                .getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT;
    }

    private static void startServerThread() {
        String mtsPath = "";
        try {
            try {
                mtsPath = new File(Loader.class.getProtectionDomain().getCodeSource().getLocation()
                                               .toURI()).getPath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            String[] command = {SteamSearch.findJRE(), "-Xms1024m", "-Xmx2048m", "-jar", "-DisServer=true", mtsPath, "--profile", "Server", "--skip-launcher", "--skip-intro"};
            // ProcessBuilder will execute process named 'CMD' and will provide '/C' and 'dir' as command line arguments to 'CMD'

            ProcessBuilder pbuilder = new ProcessBuilder(command);

            System.out.println("Starting server game");
            serverProcess = pbuilder.start();

            BufferedReader stdError = new BufferedReader(new InputStreamReader(serverProcess
                    .getErrorStream()));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(serverProcess
                    .getInputStream()));

            new Thread(() -> {
                String s = "";
                while (true) {
                    try {
                        if (!((s = stdError.readLine()) != null))
                            break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            new Thread(() -> {
                String s = "";
                while (true) {
                    try {
                        if (!((s = stdInput.readLine()) != null))
                            break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting Down External Process");
                serverProcess.destroy();
            }, "Shutdown-thread"));

//            waitForServerSuccessSignal();
            pingServer();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void pingServer() {
        new Thread(() -> {
            boolean connected = false;

            Socket socket = null;
            while (!connected) {
                try {
                    Thread.sleep(3_000);
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(HOST_IP, SERVER_GAME_PORT));
                    connected = true;
                } catch (IOException | InterruptedException e) {
//                    e.printStackTrace();
                }
            }


            while (true) {
                try {
                    Thread.sleep(500);

                    new DataOutputStream(socket.getOutputStream()).writeUTF("ping");
                    DataInputStream in = new DataInputStream(new BufferedInputStream(socket
                            .getInputStream()));
                    JsonObject responseJson = new JsonParser().parse(in.readUTF())
                                                              .getAsJsonObject();

                    serverReady = responseJson.has("ready") && responseJson.get("ready")
                                                                           .getAsBoolean();

                } catch (IOException | InterruptedException e) {
                    break;
                }
            }

            // No Longer pinging
            serverReady = false;
        }).start();
    }

    private static boolean waitingForSeverStartup() {
        return !serverReady && serverProcess != null && serverProcess.isAlive();
    }

    private static boolean canSendState() {
        boolean controllerRunning = BattleAiMod.rerunController != null && !BattleAiMod.rerunController.isDone;
        return serverReady && !controllerRunning && readyForUpdate();
    }

    private static boolean readyForUpdate() {
        if (AbstractDungeon.actionManager == null || AbstractDungeon.player == null) {
            return false;
        }

        // Wait for exhaust effects to finish so they don't come back to haunt us.
        for (AbstractGameEffect effect : AbstractDungeon.effectList) {
            if (effect instanceof ExhaustCardEffect) {
                return false;
            }
        }

        if (AbstractDungeon.actionManager.turnHasEnded
                || (AbstractDungeon.actionManager.currentAction != null && AbstractDungeon.actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS)
                || !AbstractDungeon.actionManager.isEmpty()) {
            if (!(AbstractDungeon.isScreenUp
                    && (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.HAND_SELECT || AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID || AbstractDungeon.screen == AbstractDungeon.CurrentScreen.CARD_REWARD))) {
                return false;
            }
        }

        return LudicrousSpeedMod.shouldStep();
    }

    public enum ControllerMode {
        FULL_UI,
        TOP_PANEL_LAUNCHER,
        INVISIBLE
    }

    public static ControllerMode getModeOption() {
        SpireConfig config = BattleAiMod.optionsConfig;
        if (config == null || !config.has("controller_mode")) {
            return ControllerMode.FULL_UI;
        }

        return ControllerMode.valueOf(config.getString("controller_mode"));
    }

    public static void saveMode(ControllerMode mode) {
        SpireConfig config = BattleAiMod.optionsConfig;
        if (config != null) {
            config.setString("controller_mode", mode.toString());
            try {
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isEnabled() {
        return BattleAiMod.battleClientControllerMode == ControllerMode.FULL_UI;
    }
}
