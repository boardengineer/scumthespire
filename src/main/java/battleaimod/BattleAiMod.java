package battleaimod;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.TopPanelItem;
import basemod.interfaces.OnStartBattleSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostUpdateSubscriber;
import basemod.interfaces.PreUpdateSubscriber;
import battleaimod.battleai.BattleAiController;
import battleaimod.networking.AiClient;
import battleaimod.networking.AiServer;
import battleaimod.simulator.LudicrousSpeedMod;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.evacipated.cardcrawl.modthespire.ui.ModSelectWindow;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.ThoughtBubble;
import savestate.SaveState;
import savestate.SaveStateMod;
import savestate.fastobjects.ScreenShakeFast;

import java.io.IOException;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;

@SpireInitializer
public class BattleAiMod implements PostInitializeSubscriber, PostUpdateSubscriber, OnStartBattleSubscriber, PreUpdateSubscriber {
    public final static long MESSAGE_TIME_MILLIS = 1500L;

    public static String steveMessage = null;

    public static boolean forceStep = false;
    public static AiServer aiServer = null;
    public static AiClient aiClient = null;
    public static boolean shouldStartAiFromServer = false;
    public static BattleAiController battleAiController = null;
    public static SaveState saveState;
    public static boolean goFast = false;
    public static boolean shouldStartClient = false;
    public static boolean isServer;

    public BattleAiMod() {
        BaseMod.subscribe(this);
        BaseMod.subscribe(new LudicrousSpeedMod());

        // Shut off the MTS console window, It increasingly slows things down
        ModSelectWindow window = ReflectionHacks.getPrivateStatic(Loader.class, "ex");
        window.removeAll();

        CardCrawlGame.screenShake = new ScreenShakeFast();
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
            SaveStateMod.shouldGoFast = true;
            LudicrousSpeedMod.plaidMode = true;

            ReflectionHacks.setPrivateStaticFinal(BaseMod.class, "logger", new SilentLogger());

            Settings.ACTION_DUR_XFAST = 0.001F;
            Settings.ACTION_DUR_FASTER = 0.002F;
            Settings.ACTION_DUR_FAST = 0.0025F;
            Settings.ACTION_DUR_MED = 0.005F;
            Settings.ACTION_DUR_LONG = .01F;
            Settings.ACTION_DUR_XLONG = .015F;

            if (aiServer == null) {
                aiServer = new AiServer();
            }
        } else {
            Settings.MASTER_VOLUME = .0F;
        }

        CardCrawlGame.sound.update();
        setUpOptionsMenu();
    }

    public void receivePostUpdate() {
        if (steveMessage != null) {
            String messageToDisplay = " Processing... NL " + steveMessage;
            steveMessage = null;

            AbstractDungeon.effectList
                    .add(new ThoughtBubble(AbstractDungeon.player.dialogX, AbstractDungeon.player.dialogY, (float) MESSAGE_TIME_MILLIS / 1000.F, messageToDisplay, true));

        }
        if (battleAiController == null && shouldStartAiFromServer) {
            shouldStartAiFromServer = false;
            LudicrousSpeedMod.controller = battleAiController = new BattleAiController(saveState);
        }
    }

    private void setUpOptionsMenu() {
        BaseMod.addTopPanelItem(new StartAiClientTopPanel());
    }

    public class StartAiClientTopPanel extends TopPanelItem {
        public static final String ID = "battleaimod:startclient";

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

    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
//        shouldStartClient = true;
    }

    @Override
    public void receivePreUpdate() {
        if (battleAiController == null && shouldStartAiFromServer) {
            shouldStartAiFromServer = false;
            LudicrousSpeedMod.controller = battleAiController = new BattleAiController(saveState);
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
}
