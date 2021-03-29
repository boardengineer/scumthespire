package communicationmod;

import basemod.*;
import basemod.interfaces.PostDungeonUpdateSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostUpdateSubscriber;
import basemod.interfaces.PreUpdateSubscriber;
import battleai.BattleAiController;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.IntentFlashAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.CardTrailEffect;
import com.megacrit.cardcrawl.vfx.EnemyTurnEffect;
import com.megacrit.cardcrawl.vfx.PlayerTurnEffect;
import fastobjects.ScreenShakeFast;
import fastobjects.actions.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import savestate.SaveState;
import skrelpoid.superfastmode.SuperFastMode;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;

@SpireInitializer
public class CommunicationMod implements PostInitializeSubscriber, PostUpdateSubscriber, PostDungeonUpdateSubscriber, PreUpdateSubscriber {
    private static final Logger logger = LogManager.getLogger(CommunicationMod.class.getName());
    private static final String MODNAME = "Communication Mod";
    private static final String AUTHOR = "Forgotten Arbiter";
    private static final String DESCRIPTION = "This mod communicates with an external program to play Slay the Spire.";
    private static final String COMMAND_OPTION = "command";
    private static final String GAME_START_OPTION = "runAtGameStart";
    private static final String VERBOSE_OPTION = "verbose";
    private static final String INITIALIZATION_TIMEOUT_OPTION = "maxInitializationTimeout";
    private static final String DEFAULT_COMMAND = "";
    private static final long DEFAULT_TIMEOUT = 10L;
    private static final boolean DEFAULT_VERBOSITY = true;
    private static final StringBuilder inputBuffer = new StringBuilder();
    public static Stack<SaveState> saveStates = null;
    public static boolean messageReceived = false;
    public static boolean mustSendGameState = false;
    public static boolean readyForUpdate;
    private static Process listener;
    private static Thread writeThread;
    private static BlockingQueue<String> writeQueue;
    private static Thread readThread;
    private static BlockingQueue<String> readQueue;
    private static SpireConfig communicationConfig;
    private static BattleAiController battleAiController = null;
    private static HashMap<Class, Integer> framesPerClass = new HashMap<>();
    private boolean canStep = false;
    private SaveState saveState;

    public CommunicationMod() {
        BaseMod.subscribe(this);

        Settings.ACTION_DUR_XFAST = 0.01F;
        Settings.ACTION_DUR_FASTER = 0.02F;
        Settings.ACTION_DUR_FAST = 0.025F;
        Settings.ACTION_DUR_MED = 0.05F;
        Settings.ACTION_DUR_LONG = .10F;
        Settings.ACTION_DUR_XLONG = .15F;

        CardCrawlGame.screenShake = new ScreenShakeFast();

        try {
            Properties defaults = new Properties();
            defaults.put(GAME_START_OPTION, Boolean.toString(false));
            defaults.put(INITIALIZATION_TIMEOUT_OPTION, Long.toString(DEFAULT_TIMEOUT));
            defaults.put(VERBOSE_OPTION, Boolean.toString(DEFAULT_VERBOSITY));
            communicationConfig = new SpireConfig("CommunicationMod", "config", defaults);
            String command = communicationConfig.getString(COMMAND_OPTION);
            // I want this to always be saved to the file so people can set it more easily.
            if (command == null) {
                communicationConfig.setString(COMMAND_OPTION, DEFAULT_COMMAND);
                communicationConfig.save();
            }
            communicationConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initialize() {
        CommunicationMod mod = new CommunicationMod();
    }

    private static void sendMessage(String message) {
        if (writeQueue != null && writeThread.isAlive()) {
            writeQueue.add(message);
        }
    }

    private static String getSubprocessCommandString() {
        if (communicationConfig == null) {
            return "";
        }
        return communicationConfig.getString(COMMAND_OPTION).trim();
    }

    private static boolean getRunOnGameStartOption() {
        if (communicationConfig == null) {
            return false;
        }
        return communicationConfig.getBool(GAME_START_OPTION);
    }

    private static boolean getVerbosityOption() {
        if (communicationConfig == null) {
            return DEFAULT_VERBOSITY;
        }
        return communicationConfig.getBool(VERBOSE_OPTION);
    }

    private void sendGameState() {
        if (CommandExecutor.getAvailableCommands().contains("play") || CommandExecutor
                .isEndCommandAvailable() || CommandExecutor.isChooseCommandAvailable()) {
            if (battleAiController != null) {
//                if (canStep) {
                if (canStep || true) {
//                if (canStep || !battleAiController.runCommandMode) {
                    canStep = false;

                    battleAiController.step();
                }

                if (battleAiController.isDone) {
//                    System.err.println(framesPerClass);
//
//                    framesPerClass.entrySet().stream()
//                                  .sorted((first, second) -> second.getValue() - first.getValue())
//                                  .forEach(entry -> System.err.println(entry));
                    framesPerClass = new HashMap<>();
                    battleAiController = null;
                }
            }
        }
    }

    public void receivePreUpdate() {
        Settings.ACTION_DUR_XFAST = 0.001F;
        Settings.ACTION_DUR_FASTER = 0.002F;
        Settings.ACTION_DUR_FAST = 0.0025F;
        Settings.ACTION_DUR_MED = 0.005F;
        Settings.ACTION_DUR_LONG = .01F;
        Settings.ACTION_DUR_XLONG = .015F;

        SuperFastMode.deltaMultiplier = 100000000.0F;
        SuperFastMode.isInstantLerp = true;
        SuperFastMode.isDeltaMultiplied = true;
        Settings.DISABLE_EFFECTS = true;
        Iterator<AbstractGameEffect> topLevelEffects = AbstractDungeon.topLevelEffects.iterator();
        while (topLevelEffects.hasNext()) {
            AbstractGameEffect effect = topLevelEffects.next();
            effect.duration = Math.min(effect.duration, .005F);

            if (effect instanceof EnemyTurnEffect || effect instanceof PlayerTurnEffect) {
                effect.isDone = true;
            }

            if (effect instanceof CardTrailEffect) {
                topLevelEffects.remove();
            } else {
                System.out.println(effect.getClass());
                topLevelEffects.remove();
            }
        }

        AbstractDungeon.effectList.clear();

        clearActions(AbstractDungeon.actionManager.actions);
        clearActions(AbstractDungeon.actionManager.preTurnActions);

        try {
            Class actionClass = AbstractDungeon.actionManager.currentAction.getClass();
            if (!framesPerClass.containsKey(actionClass)) {
                framesPerClass.put(actionClass, 0);
            }
            framesPerClass.put(actionClass, framesPerClass.get(actionClass) + 1);
//            System.err.println(AbstractDungeon.actionManager.currentAction.getClass());
        } catch (NullPointerException e) {
//            e.printStackTrace();
//            System.err.println("there was a null though");
        }

        if (listener != null && !listener.isAlive() && writeThread != null && writeThread
                .isAlive()) {
            logger.info("Child process has died...");
            writeThread.interrupt();
            readThread.interrupt();
        }

        if (AbstractDungeon.player != null) {
            AbstractPlayer player = AbstractDungeon.player;
            float animScale = player.state.getTimeScale();
            player.state.setTimeScale(Math.min(animScale, .0001F));

            for (AbstractRelic relic : player.relics) {
                relic.flashTimer = Math.min(relic.flashTimer, .00001F);
            }
        }
    }

    public void receivePostInitialize() {
        setUpOptionsMenu();
    }

    public void receivePostUpdate() {
        if (!mustSendGameState && GameStateListener.checkForMenuStateChange()) {
            mustSendGameState = true;
        }
        if (readyForUpdate) {
            sendGameState();
            readyForUpdate = false;
        }
    }

    public void receivePostDungeonUpdate() {

        if (GameStateListener.checkForDungeonStateChange()) {
            mustSendGameState = true;
            readyForUpdate = true;
        }
        if (AbstractDungeon.getCurrRoom().isBattleOver) {
            GameStateListener.signalTurnEnd();
        }
    }

    private void setUpOptionsMenu() {
        ModPanel settingsPanel = new ModPanel();
        ModLabeledToggleButton gameStartOptionButton = new ModLabeledToggleButton(
                "Start external process at game launch",
                350, 550, Settings.CREAM_COLOR, FontHelper.charDescFont,
                getRunOnGameStartOption(), settingsPanel, modLabel -> {
        },
                modToggleButton -> {
                    if (communicationConfig != null) {
                        communicationConfig.setBool(GAME_START_OPTION, modToggleButton.enabled);
                        try {
                            communicationConfig.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        settingsPanel.addUIElement(gameStartOptionButton);

        ModLabel externalCommandLabel = new ModLabel(
                "", 350, 600, Settings.CREAM_COLOR, FontHelper.charDescFont,
                settingsPanel, modLabel -> {
            modLabel.text = String
                    .format("External Process Command: %s", getSubprocessCommandString());
        });
        settingsPanel.addUIElement(externalCommandLabel);

        ModLabel startProcessLabel = new ModLabel(
                "(Re)start external process",
                475, 700, Settings.CREAM_COLOR, FontHelper.charDescFont,
                settingsPanel, modLabel -> {
            if (listener != null && listener.isAlive()) {
                modLabel.text = "Restart external process";
            } else {
                modLabel.text = "Start external process";
            }
        });
        settingsPanel.addUIElement(startProcessLabel);

        ModButton editProcessButton = new ModButton(
                850, 650, settingsPanel, modButton -> {
        });
        settingsPanel.addUIElement(editProcessButton);

        ModLabel editProcessLabel = new ModLabel(
                "Set command (not implemented)",
                975, 700, Settings.CREAM_COLOR, FontHelper.charDescFont,
                settingsPanel, modLabel -> {
        });
        settingsPanel.addUIElement(editProcessLabel);

        ModLabeledToggleButton verbosityOption = new ModLabeledToggleButton(
                "Suppress verbose log output",
                350, 500, Settings.CREAM_COLOR, FontHelper.charDescFont,
                getVerbosityOption(), settingsPanel, modLabel -> {
        },
                modToggleButton -> {
                    if (communicationConfig != null) {
                        communicationConfig.setBool(VERBOSE_OPTION, modToggleButton.enabled);
                        try {
                            communicationConfig.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        settingsPanel.addUIElement(verbosityOption);
        BaseMod.registerModBadge(ImageMaster
                .loadImage("Icon.png"), "Communication Mod", "Forgotten Arbiter", null, settingsPanel);
        //BaseMod.addTopPanelItem(new SaveStateTopPanel());
        //BaseMod.addTopPanelItem(new LoadStateTopPanel());
        BaseMod.addTopPanelItem(new StartAIPanel());
        BaseMod.addTopPanelItem(new StepTopPanel());

    }

    private void clearActions(List<AbstractGameAction> actions) {
        for (int i = 0; i < actions.size(); i++) {
            AbstractGameAction action = actions.get(i);
            if (action instanceof WaitAction || action instanceof IntentFlashAction) {
                actions.remove(i);
                i--;
            } else if (action instanceof DrawCardAction) {
                actions.remove(i);
                actions.add(i, new DrawCardActionFast(AbstractDungeon.player, action.amount));
            } else if (action instanceof EmptyDeckShuffleAction) {
                actions.remove(i);
                actions.add(i, new EmptyDeckShuffleActionFast());
            } else if (action instanceof DiscardAction) {
                actions.remove(i);
                actions.add(i, new DiscardCardActionFast(AbstractDungeon.player, null, action.amount, false));
            } else if (action instanceof DiscardAtEndOfTurnAction) {
                actions.remove(i);
                actions.add(i, new DiscardAtEndOfTurnActionFast());
            } else if (action instanceof AnimateSlowAttackAction) {
                actions.remove(i);
                i--;
            } else if (action instanceof RollMoveAction) {
                AbstractMonster monster = ReflectionHacks
                        .getPrivate(action, RollMoveAction.class, "monster");
                actions.remove(i);
                actions.add(i, new RollMoveActionFast(monster));
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
            battleAiController = new BattleAiController(new SaveState());

            readyForUpdate = true;
            // your onclick code
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

}
