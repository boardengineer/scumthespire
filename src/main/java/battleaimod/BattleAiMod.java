package battleaimod;

import basemod.BaseMod;
import basemod.TopPanelItem;
import basemod.interfaces.PostDungeonUpdateSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostUpdateSubscriber;
import battleaimod.battleai.BattleAiController;
import battleaimod.battleai.EndCommand;
import battleaimod.battleai.StateNode;
import battleaimod.fastobjects.ScreenShakeFast;
import battleaimod.networking.AiClient;
import battleaimod.networking.AiServer;
import battleaimod.savestate.SaveState;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpireInitializer
public class BattleAiMod implements PostInitializeSubscriber, PostUpdateSubscriber, PostDungeonUpdateSubscriber {
    public static boolean mustSendGameState = false;
    public static boolean readyForUpdate;
    private static AiServer aiServer = null;
    private static AiClient aiClient = null;
    public static boolean shouldStartAiFromServer = false;
    public static BattleAiController battleAiController = null;
    private static ShowPNG showPNG;
    private static boolean canStep = false;
    public static SaveState saveState;
    public static boolean goFast = false;

    public BattleAiMod() {
        BaseMod.subscribe(this);
        BaseMod.subscribe(new SpeedController());
//        Settings.ACTION_DUR_XFAST = 0.01F;
//        Settings.ACTION_DUR_FASTER = 0.02F;
//        Settings.ACTION_DUR_FAST = 0.025F;
//        Settings.ACTION_DUR_MED = 0.05F;
//        Settings.ACTION_DUR_LONG = .10F;
//        Settings.ACTION_DUR_XLONG = .15F;

        CardCrawlGame.screenShake = new ScreenShakeFast();
    }

    public static void initialize() {
        BattleAiMod mod = new BattleAiMod();
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        int originaHeight = originalImage.getHeight();
        int originalWidth = originalImage.getWidth();

        double originalRatio = (double) originaHeight / (double) originalWidth;
        double targetRatio = (double) targetHeight / (double) targetWidth;

        int actualWidth;
        int actualHeight;

        if (originalRatio > targetRatio) {
            // match height
            actualHeight = Math.min(targetHeight, originaHeight);
            actualWidth = (int) (actualHeight / originalRatio);
        } else {
            actualWidth = Math.min(targetWidth, originalWidth);
            actualHeight = (int) (actualWidth * originalRatio);
        }

        BufferedImage resizedImage = new BufferedImage(actualWidth, actualHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, actualWidth, actualHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    public static void sendGameState() {
        if (battleAiController == null && shouldStartAiFromServer) {
            shouldStartAiFromServer = false;
            battleAiController = new BattleAiController(saveState);
        }
        if (battleAiController != null && BattleAiController.shouldStep()) {
//                if (canStep) {
            if (canStep || true) {
//                if (canStep || !battleAiController.runCommandMode) {
                canStep = false;

                battleAiController.step();
            }

            if (battleAiController.isDone) {
                battleAiController = null;
            }
        }
    }

    public void receivePostInitialize() {
        setUpOptionsMenu();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            showPNG = new ShowPNG("C:/Users/pasha/out.png");
            showPNG.setVisible(true);
            while (true) {
                try {
                    if (showPNG != null) {
                        showPNG.loadImage();
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void receivePostUpdate() {
        if (!mustSendGameState && GameStateListener.checkForMenuStateChange()) {
            mustSendGameState = true;
        }

    }

    public void receivePostDungeonUpdate() {
        if (GameStateListener.checkForDungeonStateChange()) {
            mustSendGameState = true;
            if (AbstractDungeon.actionManager != null && AbstractDungeon.actionManager.phase == GameActionManager.Phase.WAITING_ON_USER) {
                readyForUpdate = true;
            } else {
                System.err.println("but the action manager is doing stuff");
            }

        }
        if (AbstractDungeon.getCurrRoom().isBattleOver) {
            GameStateListener.signalTurnEnd();
        }
    }

    private void setUpOptionsMenu() {
//        BaseMod.addTopPanelItem(new StartAIPanel());

        BaseMod.addTopPanelItem(new StartAiServerTopPanel());
        BaseMod.addTopPanelItem(new StartAiClientTopPanel());

//        BaseMod.addTopPanelItem(new SaveStateTopPanel());
//        BaseMod.addTopPanelItem(new LoadStateTopPanel());
    }

    @SuppressWarnings("serial")
    static class ShowPNG extends JFrame {
        private final String path;
        private final JLabel label;

        public ShowPNG(String path) {
            this.path = path;
            this.label = new JLabel();
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setSize(500, 640);
            this.getContentPane().add(label);
            loadImage();
        }

        public void loadImage() {
            try {
                String textPath = "C:\\Users\\pasha\\test.dot";
                if (battleAiController.root != null) {

                    try {
                        FileWriter myWriter = new FileWriter(textPath);

                        String graphString = "digraph G {\n";
                        Stack<StateNode> toVisit = new Stack<>();
                        toVisit.add(battleAiController.root);
                        while (!toVisit.isEmpty()) {
                            StateNode node = toVisit.pop();
                            toVisit.addAll(node.children.values());

                            StateNode iterator = node.parent;
                            if (iterator != null) {
                                while (iterator.parent != null && iterator
                                        .getLastCommand() != null && !(iterator
                                        .getLastCommand() instanceof EndCommand)) {
                                    iterator = iterator.parent;
                                }
                            }

                            if (node.parent != null) {
                                graphString +=
                                        String.format("%d->%d[label=\"%s\"]\n", iterator.stateNumber, node.stateNumber, node.stateString);
                            }
                        }
                        graphString += "}";

                        myWriter.write(graphString);
                        myWriter.close();
                    } catch (IOException e) {
                        System.err.println("failed to write");
                    }


                }
                Runtime.getRuntime()
                       .exec(new String[]{"dot", textPath, "-o", path, "-T", "png"});

                BufferedImage bufImg = resizeImage(ImageIO
                        .read(new File(path)), getWidth(), getHeight());
                label.setIcon(new ImageIcon(bufImg));
            } catch (IOException e) {
            } catch (NullPointerException e) {
            }
        }
    }

    public class SaveStateTopPanel extends TopPanelItem {
        public static final String ID = "yourmodname:SaveState";

        public SaveStateTopPanel() {
            super(new Texture("save.png"), ID);
        }

        @Override
        protected void onClick() {
            System.out.println("you clicked on save");
            saveState = new SaveState();

            readyForUpdate = true;
        }
    }

    public class StartAIPanel extends TopPanelItem {
        public static final String ID = "yourmodname:SaveState";

        public StartAIPanel() {
            super(new Texture("save.png"), ID);
        }

        @Override
        protected void onClick() {
            battleAiController = new BattleAiController(new SaveState(), true);

            readyForUpdate = true;
        }
    }

    public class LoadStateTopPanel extends TopPanelItem {
        public static final String ID = "yourmodname:LoadState";

        public LoadStateTopPanel() {
            super(new Texture("Icon.png"), ID);
        }

        @Override
        protected void onClick() {
            readyForUpdate = true;
            receivePostUpdate();

            if (saveState != null) {
                saveState.loadState();
            }
        }
    }

    public class StepTopPanel extends TopPanelItem {
        public static final String ID = "yourmodname:Step";

        public StepTopPanel() {
            super(new Texture("Icon.png"), ID);
        }

        @Override
        protected void onClick() {
            canStep = true;
            readyForUpdate = true;
            receivePostUpdate();
        }
    }

    public class StartAiClientTopPanel extends TopPanelItem {
        public static final String ID = "yourmodname:Step";

        public StartAiClientTopPanel() {
            super(new Texture("Icon.png"), ID);
        }

        @Override
        protected void onClick() {
            if (aiClient == null) {
                try {
                    aiClient = new AiClient();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (aiClient != null) {
                aiClient.sendState();
            }
        }
    }

    public class StartAiServerTopPanel extends TopPanelItem {
        public static final String ID = "yourmodname:startAi";

        public StartAiServerTopPanel() {
            super(new Texture("save.png"), ID);
        }

        @Override
        protected void onClick() {
            if (aiServer == null) {
                aiServer = new AiServer();
            }
        }
    }
}
