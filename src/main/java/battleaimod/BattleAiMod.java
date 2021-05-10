package battleaimod;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.TopPanelItem;
import basemod.interfaces.*;
import battleaimod.battleai.BattleAiController;
import battleaimod.battleai.commands.CardCommand;
import battleaimod.fastobjects.ScreenShakeFast;
import battleaimod.networking.AiClient;
import battleaimod.networking.AiServer;
import battleaimod.savestate.SaveState;
import battleaimod.savestate.actions.Action;
import battleaimod.savestate.actions.CurrentAction;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.orbs.Orb;
import battleaimod.savestate.powers.Power;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.evacipated.cardcrawl.modthespire.ui.ModSelectWindow;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.ThoughtBubble;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;

@SpireInitializer
public class BattleAiMod implements PostInitializeSubscriber, PostUpdateSubscriber, PostDungeonUpdateSubscriber, OnStartBattleSubscriber, PreUpdateSubscriber {
    public final static long MESSAGE_TIME_MILLIS = 1500L;

    public static String steveMessage = null;

    public static boolean mustSendGameState = false;
    public static boolean readyForUpdate;
    public static boolean forceStep = false;
    public static AiServer aiServer = null;
    public static AiClient aiClient = null;
    public static boolean shouldStartAiFromServer = false;
    public static BattleAiController battleAiController = null;
    private static boolean canStep = false;
    public static SaveState saveState;
    public static boolean goFast = false;
    public static boolean shouldStartClient = false;
    public static long logCounter = 0;
    public static boolean isServer;

    public static HashMap<String, Monster> monsterByIdmap;
    public static HashMap<String, Power> powerByIdmap;

    public static HashMap<Class, Action> actionByClassMap;
    public static HashMap<Class, CurrentAction> currentActionByClassMap;

    public static HashMap<Class, Orb> orbByClassMap;

    public BattleAiMod() {
        BaseMod.subscribe(this);
        BaseMod.subscribe(new SpeedController());

        // Shut off the MTS console window, It increasingly slows things down
        ModSelectWindow window = ReflectionHacks.getPrivateStatic(Loader.class, "ex");
        window.removeAll();

//        Settings.ACTION_DUR_XFAST = 0.01F;
//        Settings.ACTION_DUR_FASTER = 0.02F;
//        Settings.ACTION_DUR_FAST = 0.025F;
//        Settings.ACTION_DUR_MED = 0.05F;
//        Settings.ACTION_DUR_LONG = .10F;
//        Settings.ACTION_DUR_XLONG = .15F;

        CardCrawlGame.screenShake = new ScreenShakeFast();
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
        } else if (!BattleAiController.shouldStep()) {
            System.err.println("Can't step yet");
        }
    }

    public static void initialize() {
        BattleAiMod mod = new BattleAiMod();
        monsterByIdmap = new HashMap<>();
        powerByIdmap = new HashMap<>();
        actionByClassMap = new HashMap<>();
        currentActionByClassMap = new HashMap<>();
        orbByClassMap = new HashMap<>();

        for (Monster monster : Monster.values()) {
            monsterByIdmap.put(monster.monsterId, monster);
        }

        for (Power power : Power.values()) {
            powerByIdmap.put(power.powerId, power);
        }

        for (Action action : Action.values()) {
            actionByClassMap.put(action.actionClass, action);
        }

        for (CurrentAction action : CurrentAction.values()) {
            currentActionByClassMap.put(action.actionClass, action);
        }

        for (Orb orb : Orb.values()) {
            orbByClassMap.put(orb.orbClass, orb);
        }
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

    public void receivePostInitialize() {
        String isServerFlag = System.getProperty("isServer");

        if (isServerFlag != null) {
            if (Boolean.parseBoolean(isServerFlag)) {
                BattleAiMod.isServer = true;

            }
        }

        if (isServer) {
            Settings.MASTER_VOLUME = 0;
            Settings.isDemo = true;
            goFast = true;
        } else {
            Settings.MASTER_VOLUME = .0F;
        }

        CardCrawlGame.sound.update();
        setUpOptionsMenu();
    }

    public void receivePostUpdate() {
        if (++BattleAiMod.logCounter % 300 == 0) {
//            System.err.println(BattleAiMod.logCounter);
        }
        if (steveMessage != null) {
            String messageToDisplay = " Processing... NL " + steveMessage;
            steveMessage = null;

            AbstractDungeon.effectList
                    .add(new ThoughtBubble(AbstractDungeon.player.dialogX, AbstractDungeon.player.dialogY, (float) MESSAGE_TIME_MILLIS / 1000.F, messageToDisplay, true));

        }
        if (battleAiController == null && shouldStartAiFromServer) {
            shouldStartAiFromServer = false;
            battleAiController = new BattleAiController(saveState);
            readyForUpdate = true;
        }
        if (!mustSendGameState && GameStateListener.checkForMenuStateChange()) {
            mustSendGameState = true;
        }
    }

    public void receivePostDungeonUpdate() {
        if (GameStateListener.checkForDungeonStateChange()) {
            mustSendGameState = true;
            if (AbstractDungeon.actionManager != null && AbstractDungeon.actionManager.phase == GameActionManager.Phase.WAITING_ON_USER) {
                readyForUpdate = true;
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
            new CardCommand(4, "").execute();
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

    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
//        shouldStartClient = true;
    }

    @Override
    public void receivePreUpdate() {
        if (actionManager.actions.isEmpty() && actionManager.currentAction == null) {
            if (shouldStartClient) {
                shouldStartClient = false;
                AbstractDungeon.effectList
                        .add(new ThoughtBubble(AbstractDungeon.player.dialogX, AbstractDungeon.player.dialogY, 2.0F, "Hello World", true));

                actionManager.actions.add(new WaitAction(2.0F));
                actionManager.actions.add(new AbstractGameAction() {
                    @Override
                    public void update() {
                        AbstractDungeon.effectList
                                .add(new ThoughtBubble(AbstractDungeon.player.dialogX, AbstractDungeon.player.dialogY, 3.0F, "Here we go", true));

                        if (BattleAiMod.aiClient == null) {
                            try {
                                BattleAiMod.aiClient = new AiClient();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        isDone = true;

                        if (BattleAiMod.aiClient != null) {
                            BattleAiMod.aiClient.sendState();
                        }
                    }
                });
            }
        }
    }
}
