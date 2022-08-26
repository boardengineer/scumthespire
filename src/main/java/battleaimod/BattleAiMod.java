package battleaimod;

import basemod.*;
import basemod.eventUtil.EventUtils;
import basemod.interfaces.*;
import basemod.patches.com.megacrit.cardcrawl.helpers.PotionLibrary.PotionHelperGetPotion;
import battleaimod.battleai.BattleAiController;
import battleaimod.battleai.CommandRunnerController;
import battleaimod.battleai.playorder.*;
import battleaimod.networking.AiClient;
import battleaimod.networking.AiServer;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.evacipated.cardcrawl.modthespire.steam.SteamSearch;
import com.evacipated.cardcrawl.modthespire.ui.ModSelectWindow;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.common.ExhaustAction;
import com.megacrit.cardcrawl.actions.unique.DualWieldAction;
import com.megacrit.cardcrawl.actions.unique.NightmareAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.audio.MainMusic;
import com.megacrit.cardcrawl.audio.Sfx;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.colorless.Forethought;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Lagavulin;
import com.megacrit.cardcrawl.monsters.exordium.SlimeBoss;
import com.megacrit.cardcrawl.relics.*;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.vfx.ThoughtBubble;
import ludicrousspeed.LudicrousSpeedMod;
import ludicrousspeed.simulator.commands.GridSelectConfrimCommand;
import ludicrousspeed.simulator.commands.HandSelectCommand;
import ludicrousspeed.simulator.commands.HandSelectConfirmCommand;
import savestate.PotionState;
import savestate.SaveState;
import savestate.SaveStateMod;
import savestate.fastobjects.ScreenShakeFast;

import java.io.File;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;
import static java.lang.Thread.currentThread;
import static ludicrousspeed.LudicrousSpeedMod.controller;
import static ludicrousspeed.LudicrousSpeedMod.plaidMode;

@SpireInitializer
public class BattleAiMod implements PostInitializeSubscriber, PostUpdateSubscriber, OnStartBattleSubscriber, PreUpdateSubscriber, EditRelicsSubscriber {
    public final static long MESSAGE_TIME_MILLIS = 1500L;
    private static final int SERVER_GAME_PORT = 5124;

    public static String steveMessage = null;

    public static boolean forceStep = false;
    public static AiServer aiServer = null;
    public static AiClient aiClient = null;
    public static boolean shouldStartAiFromServer = false;
    public static BattleAiController battleAiController = null;

    public static CommandRunnerController rerunController = null;

    public static SaveState saveState;
    public static int requestedTurnNum;
    public static boolean goFast = false;
    public static boolean shouldStartClient = false;

    public static boolean isServer;
    public static boolean isClient;

    public static ArrayList<Comparator<AbstractCard>> cardPlayHeuristics = new ArrayList<>();
    public static HashMap<Class, Comparator<AbstractCard>> actionHeuristics = new HashMap<>();

    public static ArrayList<Function<SaveState, Integer>> additionalValueFunctions = new ArrayList<>();

    static ServerSocket serverGameServerSocket = null;
    static Socket serverGameSocket = null;

    public BattleAiMod() {
        BaseMod.subscribe(this);
        BaseMod.subscribe(new LudicrousSpeedMod());

        // Shut off the MTS console window, It increasingly slows things down
        ModSelectWindow window = ReflectionHacks.getPrivateStatic(Loader.class, "ex");
        window.removeAll();

        CardCrawlGame.screenShake = new ScreenShakeFast();

        cardPlayHeuristics.add(IronCladPlayOrder.COMPARATOR);
        cardPlayHeuristics.add(DefectPlayOrder.COMPARATOR);
        cardPlayHeuristics.add(SilentPlayOrder.COMPARATOR);

        actionHeuristics.put(DiscardAction.class, DiscardOrder.COMPARATOR);
        actionHeuristics.put(ExhaustAction.class, ExhaustOrder.COMPARATOR);

        actionHeuristics.put(NightmareAction.class, new BadCardsLastHeuristic());
        actionHeuristics.put(DualWieldAction.class, new BadCardsLastHeuristic());
    }

    public static void sendGameState() {
        if (battleAiController != null) {
            battleAiController.step();

            if (battleAiController.isDone()) {
                battleAiController = null;
            }
        }
    }

    public static void initialize() {
        BattleAiMod mod = new BattleAiMod();
    }

    @Override
    public void receivePostInitialize() {
        // Sometimes doesn't come back to hand for some reason
        CardLibrary.cards.remove(Forethought.ID);

        // Current behavior would make this a chat opti on, it won't be interesting out of the box
        HashMap<String, AbstractRelic> sharedRelics = ReflectionHacks
                .getPrivateStatic(RelicLibrary.class, "sharedRelics");
        sharedRelics.remove(GamblingChip.ID);

        Iterator<String> actualPotions = PotionHelper.potions.iterator();
        while (actualPotions.hasNext()) {
            String potionId = actualPotions.next();
            for (String toRemove : PotionState.UNPLAYABLE_POTIONS) {
                if (potionId.equals(toRemove)) {
                    actualPotions.remove();
                    continue;
                }
            }
        }

        String isServerFlag = System.getProperty("isServer");

        if (isServerFlag != null) {
            if (Boolean.parseBoolean(isServerFlag)) {
                BattleAiMod.isServer = true;
            }
        }

        String isClientFlag = System.getProperty("isClient");

        if (isClientFlag != null) {
            if (Boolean.parseBoolean(isClientFlag)) {
                BattleAiMod.isClient = true;
            }
        }

        ReflectionHacks.setPrivateStaticFinal(MummifiedHand.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(BaseMod.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(TheSpecimen.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(AbstractDungeon.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(Lagavulin.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(SlimeBoss.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(CardGroup.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(CardHelper.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(UnlockTracker.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(ImageMaster.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(AbstractMonster.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(MainMusic.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(AbstractPlayer.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(Sfx.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(PotionHelper.class, "logger", new SilentLogger());
        ReflectionHacks
                .setPrivateStaticFinal(PotionHelperGetPotion.class, "logger", new SilentLogger());


        ReflectionHacks.setPrivateStaticFinal(EventUtils.class, "eventLogger", new SilentLogger());

        if (isServer) {
            Settings.MASTER_VOLUME = 0;
            Settings.isDemo = true;
            goFast = true;
            SaveStateMod.shouldGoFast = true;
            plaidMode = true;

            Settings.ACTION_DUR_XFAST = 0.001F;
            Settings.ACTION_DUR_FASTER = 0.002F;
            Settings.ACTION_DUR_FAST = 0.0025F;
            Settings.ACTION_DUR_MED = 0.005F;
            Settings.ACTION_DUR_LONG = .01F;
            Settings.ACTION_DUR_XLONG = .015F;

            if (aiServer == null) {
                aiServer = new AiServer();
            }
        } else if (isClient) {
            Settings.MASTER_VOLUME = .7F;
        } else {
            Settings.MASTER_VOLUME = .0F;
        }

        CardCrawlGame.sound.update();
        setUpOptionsMenu();
    }

    @Override
    public void receivePostUpdate() {
        if (steveMessage != null) {
            String messageToDisplay = " Processing... NL " + steveMessage;
            steveMessage = null;

            AbstractDungeon.effectList
                    .add(new ThoughtBubble(AbstractDungeon.player.dialogX, AbstractDungeon.player.dialogY, (float) MESSAGE_TIME_MILLIS / 1000.F, messageToDisplay, true));

        }
        if (battleAiController == null && shouldStartAiFromServer) {
            shouldStartAiFromServer = false;
            controller = battleAiController = new BattleAiController(saveState, requestedTurnNum);
        }
    }

    private void setUpOptionsMenu() {
        BaseMod.addTopPanelItem(new StartAiClientTopPanel());

        ModPanel settingsPanel = new ModPanel();


        final Thread masterThread = currentThread();

        ModButton startProcessButton = new ModButton(
                350, 650, settingsPanel, modButton -> {
            ThreadFactory namedThreadFactory =
                    new ThreadFactoryBuilder().setNameFormat("server-thread-%d").build();
            ExecutorService executor = Executors.newSingleThreadExecutor(namedThreadFactory);
            executor.submit(() -> {
                BaseMod.modSettingsUp = false;
                startExternalProcess(masterThread);
            });
        });
        settingsPanel.addUIElement(startProcessButton);

        ModLabel startProcessLabel = new ModLabel(
                "(Re)start external process",
                475, 700, Settings.CREAM_COLOR, FontHelper.charDescFont,
                settingsPanel, modLabel -> {
            modLabel.text = "Start external process";
        });
        settingsPanel.addUIElement(startProcessLabel);

        BaseMod.registerModBadge(ImageMaster
                .loadImage("Icon.png"), "Battle Ai Mod", "Board Engineer", "Plays the Battle for yourself", settingsPanel);
    }

    private void startExternalProcess(Thread masterThread) {
        String mtsPath = "";
        try {
            try {
                mtsPath = new File(Loader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            String[] command = {SteamSearch.findJRE(), "-Xms1024m", "-Xmx2048m", "-jar", "-DisServer=true", mtsPath, "--profile", "Server", "--skip-launcher", "--skip-intro"};
            // ProcessBuilder will execute process named 'CMD' and will provide '/C' and 'dir' as command line arguments to 'CMD'

            ProcessBuilder pbuilder = new ProcessBuilder(command);

            System.out.println("Starting server game");
            Process process = pbuilder.start();

            BufferedReader stdError = new BufferedReader(new InputStreamReader(process
                    .getErrorStream()));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process
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

                    System.err.println(s);
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
                    System.out.println(s);
                }
            }).start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting Down External Process");
                process.destroy();
            }, "Shutdown-thread"));

            waitForServerSuccessSignal();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void waitForServerSuccessSignal() {
        new Thread(() -> {
            try {
                if (serverGameServerSocket != null) {
                    serverGameServerSocket.close();
                }

                if (serverGameSocket != null) {
                    serverGameSocket.close();
                }

                System.out.println("Waiting for server to start... ");
                serverGameServerSocket = new ServerSocket(SERVER_GAME_PORT);

                serverGameSocket = serverGameServerSocket.accept();

                DataInputStream serverInputStream = new DataInputStream(new BufferedInputStream(serverGameSocket
                        .getInputStream()));
                DataOutputStream serverOutputStream = new DataOutputStream(serverGameSocket
                        .getOutputStream());

                System.out.println("Waiting for game to start...");

                String serverResponse = serverInputStream.readUTF();

                System.out.println("server wrote " + serverResponse);
                if (serverResponse.equals("SUCCESS")) {
                    System.out.println("we did it!!!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public class StartAiClientTopPanel extends TopPanelItem {
        public static final String ID = "battleaimod:startclient";

        public StartAiClientTopPanel() {
            super(new Texture("img/StartSteve.png"), ID);
        }

        @Override
        protected void onClick() {
//            if (aiClient == null) {
//                try {
//                    aiClient = new AiClient(false);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

//            aiClient.sendState("C:\\stuff\\_ModTheSpire\\startstates\\5F6NSV520STJZ\\40\\01\\commands.txt");

//            aiClient.sendState();


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

    public class TryButtonPanel extends TopPanelItem {
        public static final String ID = "battleaimod:tryButton";

        public TryButtonPanel() {
            super(new Texture("img/ClimbLives.png"), ID);
        }

        @Override
        protected void onClick() {
            new HandSelectCommand(0).execute();
        }
    }

    public class TryButtonPanel2 extends TopPanelItem {
        public static final String ID = "battleaimod:tryButton";

        public TryButtonPanel2() {
            super(new Texture("img/ClimbLives.png"), ID);
        }

        @Override
        protected void onClick() {
            HandSelectConfirmCommand.INSTANCE.execute();
        }
    }

    public class TryButtonPanel3 extends TopPanelItem {
        public static final String ID = "battleaimod:tryButton";

        public TryButtonPanel3() {
            super(new Texture("img/ClimbLives.png"), ID);
        }

        @Override
        protected void onClick() {
            GridSelectConfrimCommand.INSTANCE.execute();
        }
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
//        shouldStartClient = true;
    }

    @Override
    public void receivePreUpdate() {

//        if (controller != null && controller.isDone()) {
//            controller = null;
//        }


        if (battleAiController == null && shouldStartAiFromServer) {
            shouldStartAiFromServer = false;
            battleAiController = new BattleAiController(saveState, requestedTurnNum);
            controller = battleAiController;
        }

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

    @Override
    public void receiveEditRelics() {
        // Skipping the card seems to kick the player back to character select (the main menu?)
        // for some reason.
        BaseMod.removeRelic(new TinyHouse());
    }
}
