//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package battleaimod;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.TopPanelItem;
import basemod.eventUtil.EventUtils;
import basemod.interfaces.OnStartBattleSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostUpdateSubscriber;
import basemod.interfaces.PreUpdateSubscriber;
import basemod.interfaces.RenderSubscriber;
import basemod.patches.com.megacrit.cardcrawl.helpers.PotionLibrary.PotionHelperGetPotion;
import battleaimod.battleai.BattleAiController;
import battleaimod.battleai.CommandRunnerController;
import battleaimod.battleai.evolution.EvolutionManager;
import battleaimod.battleai.evolution.FitnessFunctionEvaluator;
import battleaimod.battleai.playorder.BadCardsLastHeuristic;
import battleaimod.battleai.playorder.DefectPlayOrder;
import battleaimod.battleai.playorder.DiscardOrder;
import battleaimod.battleai.playorder.ExhaustOrder;
import battleaimod.battleai.playorder.IronCladPlayOrder;
import battleaimod.battleai.playorder.SilentPlayOrder;
import battleaimod.networking.AiClient;
import battleaimod.networking.AiServer;
import battleaimod.networking.AutoPlayController;
import battleaimod.networking.BattleClientController;
import battleaimod.networking.BattleClientController.ControllerMode;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.evacipated.cardcrawl.modthespire.ui.ModSelectWindow;
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
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.AbstractPlayer.PlayerClass;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardHelper;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Lagavulin;
import com.megacrit.cardcrawl.monsters.exordium.SlimeBoss;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.MummifiedHand;
import com.megacrit.cardcrawl.relics.TheSpecimen;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.vfx.ThoughtBubble;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;
import ludicrousspeed.LudicrousSpeedMod;
import ludicrousspeed.simulator.commands.GridSelectConfrimCommand;
import ludicrousspeed.simulator.commands.HandSelectCommand;
import ludicrousspeed.simulator.commands.HandSelectConfirmCommand;
import org.apache.logging.log4j.Level;
import savestate.PotionState;
import savestate.SaveState;
import savestate.SaveStateMod;
import savestate.fastobjects.ScreenShakeFast;

@SpireInitializer
public class BattleAiMod implements PostInitializeSubscriber, PostUpdateSubscriber, OnStartBattleSubscriber, PreUpdateSubscriber, RenderSubscriber {
    public static final HashMap<AbstractPlayer.PlayerClass, String> MESSAGE_WORDS = new HashMap<AbstractPlayer.PlayerClass, String>() {
        {
            this.put(PlayerClass.IRONCLAD, "Strategizing");
            this.put(PlayerClass.THE_SILENT, "Scheming");
            this.put(PlayerClass.WATCHER, "Foreseeing");
            this.put(PlayerClass.DEFECT, "Processing");
        }
    };
    public static final long MESSAGE_TIME_MILLIS = 1500L;
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
    public static BattleClientController clientController;
    public static BattleClientController.ControllerMode battleClientControllerMode;
    public static ArrayList<Comparator<AbstractCard>> cardPlayHeuristics = new ArrayList();
    public static HashMap<Class, Comparator<AbstractCard>> actionHeuristics = new HashMap();
    public static ArrayList<Function<SaveState, Integer>> additionalValueFunctions = new ArrayList();
    static ServerSocket serverGameServerSocket = null;
    static Socket serverGameSocket = null;
    public static SpireConfig optionsConfig;

    public static EvolutionManager evolutionManager;

    public BattleAiMod() {
        BaseMod.subscribe(this);
        BaseMod.subscribe(new LudicrousSpeedMod());
        BaseMod.subscribe(new AutoPlayController());

        evolutionManager = new EvolutionManager();
        BaseMod.subscribe(evolutionManager);

        BaseMod.subscribe(new FitnessFunctionEvaluator());

        BaseMod.logger.log(Level.INFO, "BattleAIMod constructor");

        try {
            optionsConfig = new SpireConfig("BattleAIMod", "options");
        } catch (IOException e) {
            e.printStackTrace();
        }

        ModSelectWindow window = (ModSelectWindow)ReflectionHacks.getPrivateStatic(Loader.class, "ex");
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
        new BattleAiMod();
    }

    public void receivePostInitialize() {
        CardLibrary.cards.remove("Forethought");
        HashMap<String, AbstractRelic> sharedRelics = (HashMap)ReflectionHacks.getPrivateStatic(RelicLibrary.class, "sharedRelics");
        sharedRelics.remove("Gambling Chip");
        Iterator<String> actualPotions = PotionHelper.potions.iterator();

        while(actualPotions.hasNext()) {
            String potionId = (String)actualPotions.next();

            for(String toRemove : PotionState.UNPLAYABLE_POTIONS) {
                if (potionId.equals(toRemove)) {
                    actualPotions.remove();
                }
            }
        }

        String isServerFlag = System.getProperty("isServer");
        if (isServerFlag != null && Boolean.parseBoolean(isServerFlag)) {
            isServer = true;
        }

        String isClientFlag = System.getProperty("isClient");
        if (isClientFlag != null && Boolean.parseBoolean(isClientFlag)) {
            isClient = true;
        }

        CardCrawlGame.displayVersion = false;
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
        ReflectionHacks.setPrivateStaticFinal(PotionHelperGetPotion.class, "logger", new SilentLogger());
        ReflectionHacks.setPrivateStaticFinal(EventUtils.class, "eventLogger", new SilentLogger());
        if (isServer) {
            Settings.MASTER_VOLUME = 0.0F;
            Settings.isDemo = true;
            goFast = true;
            SaveStateMod.shouldGoFast = true;
            LudicrousSpeedMod.plaidMode = true;
            Settings.ACTION_DUR_XFAST = 0.001F;
            Settings.ACTION_DUR_FASTER = 0.002F;
            Settings.ACTION_DUR_FAST = 0.0025F;
            Settings.ACTION_DUR_MED = 0.005F;
            Settings.ACTION_DUR_LONG = 0.01F;
            Settings.ACTION_DUR_XLONG = 0.015F;
            if (aiServer == null) {
                aiServer = new AiServer();
            }
        }

        CardCrawlGame.sound.update();
        clientController = new BattleClientController();
        battleClientControllerMode = BattleClientController.getModeOption();


        BaseMod.addTopPanelItem(new StartAiClientTopPanel());
        BaseMod.registerModBadge(ImageMaster.loadImage("Icon.png"), "Battle Ai Mod", "Board Engineer", "Plays the Battle for yourself", new BattleAiModOptionsPanel());


    }

    public void receivePostUpdate() {

        if (battleAiController == null && shouldStartAiFromServer) {
            shouldStartAiFromServer = false;
            LudicrousSpeedMod.controller = battleAiController = new BattleAiController(saveState, requestedTurnNum);
        }

        clientController.update();
    }

    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
    }

    public void receivePreUpdate() {
        if (battleAiController == null && shouldStartAiFromServer) {
            shouldStartAiFromServer = false;
            battleAiController = new BattleAiController(saveState, requestedTurnNum);
            LudicrousSpeedMod.controller = battleAiController;
        }

        if (AbstractDungeon.actionManager.actions.isEmpty() && AbstractDungeon.actionManager.currentAction == null && shouldStartClient) {
            shouldStartClient = false;
            AbstractDungeon.effectList.add(new ThoughtBubble(AbstractDungeon.player.dialogX, AbstractDungeon.player.dialogY, 2.0F, "Hello World", true));
            AbstractDungeon.actionManager.actions.add(new WaitAction(2.0F));
            AbstractDungeon.actionManager.actions.add(new AbstractGameAction() {
                public void update() {
                    AbstractDungeon.effectList.add(new ThoughtBubble(AbstractDungeon.player.dialogX, AbstractDungeon.player.dialogY, 3.0F, "Here we go", true));
                    if (BattleAiMod.aiClient == null) {
                        try {
                            BattleAiMod.aiClient = new AiClient();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    this.isDone = true;
                    if (BattleAiMod.aiClient != null) {
                        BattleAiMod.aiClient.sendState();
                    }

                }
            });
        }

    }

    public void receiveRender(SpriteBatch spriteBatch) {
        clientController.render(spriteBatch);
    }

    public class StartAiClientTopPanel extends TopPanelItem {
        public static final String ID = "battleaimod:startclient";

        public StartAiClientTopPanel() {
            super(new Texture("img/StartSteve.png"), "battleaimod:startclient");
        }

        protected void onClick() {
            if (this.isEnabled()) {
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

        public void update() {
            if (this.isEnabled()) {
                super.update();
            }

        }

        public void render(SpriteBatch sb) {
            if (this.isEnabled()) {
                super.render(sb);
            }

        }

        private boolean isEnabled() {
            return BattleAiMod.battleClientControllerMode == ControllerMode.TOP_PANEL_LAUNCHER;
        }
    }

    public class TryButtonPanel extends TopPanelItem {
        public static final String ID = "battleaimod:tryButton";

        public TryButtonPanel() {
            super(new Texture("img/ClimbLives.png"), "battleaimod:tryButton");
        }

        protected void onClick() {
            (new HandSelectCommand(0)).execute();
        }
    }

    public class TryButtonPanel2 extends TopPanelItem {
        public static final String ID = "battleaimod:tryButton";

        public TryButtonPanel2() {
            super(new Texture("img/ClimbLives.png"), "battleaimod:tryButton");
        }

        protected void onClick() {
            HandSelectConfirmCommand.INSTANCE.execute();
        }
    }

    public class TryButtonPanel3 extends TopPanelItem {
        public static final String ID = "battleaimod:tryButton";

        public TryButtonPanel3() {
            super(new Texture("img/ClimbLives.png"), "battleaimod:tryButton");
        }

        protected void onClick() {
            GridSelectConfrimCommand.INSTANCE.execute();
        }
    }
}
