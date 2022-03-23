package autoplay.battleaimod.server;

import autoplay.battleaimod.server.battleai.BattleAiController;
import autoplay.battleaimod.server.battleai.playorder.*;
import autoplay.battleaimod.server.networking.AiServer;
import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.interfaces.EditRelicsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostUpdateSubscriber;
import basemod.interfaces.PreUpdateSubscriber;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.evacipated.cardcrawl.modthespire.ui.ModSelectWindow;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.common.ExhaustAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Forethought;
import com.megacrit.cardcrawl.cards.purple.Weave;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.GamblingChip;
import com.megacrit.cardcrawl.relics.TinyHouse;
import ludicrousspeed.LudicrousSpeedMod;
import savestate.PotionState;
import savestate.SaveState;
import savestate.SaveStateMod;
import savestate.fastobjects.ScreenShakeFast;

import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

@SpireInitializer
public class BattleAiServerMod implements PostInitializeSubscriber, PostUpdateSubscriber, PreUpdateSubscriber, EditRelicsSubscriber {
    public final static long MESSAGE_TIME_MILLIS = 1500L;
    public static AiServer aiServer = null;
    public static boolean shouldStartAiFromServer = false;
    public static BattleAiController battleAiController = null;

    public static SaveState saveState;
    public static int requestedTurnNum;
    public static boolean goFast = false;


    public static ArrayList<Comparator<AbstractCard>> cardPlayHeuristics = new ArrayList<>();
    public static HashMap<Class, Comparator<AbstractCard>> actionHeuristics = new HashMap<>();

    public static ArrayList<Function<SaveState, Integer>> additionalValueFunctions = new ArrayList<>();

    public BattleAiServerMod() {
        BaseMod.subscribe(this);
        BaseMod.subscribe(new LudicrousSpeedMod());

        // Shut off the MTS console window, It increasingly slows things down
        ModSelectWindow window = ReflectionHacks.getPrivateStatic(Loader.class, "ex");
        window.removeAll();
        window.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        window.setVisible(false);
        Loader.closeWindow();

        CardCrawlGame.screenShake = new ScreenShakeFast();

        cardPlayHeuristics.add(IronCladPlayOrder.COMPARATOR);
        cardPlayHeuristics.add(DefectPlayOrder.COMPARATOR);
        cardPlayHeuristics.add(SilentPlayOrder.COMPARATOR);

        actionHeuristics.put(DiscardAction.class, DiscardOrder.COMPARATOR);
        actionHeuristics.put(ExhaustAction.class, ExhaustOrder.COMPARATOR);
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
        BattleAiServerMod mod = new BattleAiServerMod();
    }

    @Override
    public void receivePostInitialize() {
        // Sometimes doesn't come back to hand for some reason
        CardLibrary.cards.remove(Weave.ID);
        CardLibrary.cards.remove(Forethought.ID);

        // Current behavior would make this a chat option, it won't be interesting out of the box
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



        Settings.MASTER_VOLUME = 0;
        Settings.isDemo = true;
        goFast = true;
        SaveStateMod.shouldGoFast = true;
        LudicrousSpeedMod.plaidMode = true;


        Settings.ACTION_DUR_XFAST = 0.001F;
        Settings.ACTION_DUR_FASTER = 0.002F;
        Settings.ACTION_DUR_FAST = 0.0025F;
        Settings.ACTION_DUR_MED = 0.005F;
        Settings.ACTION_DUR_LONG = .01F;
        Settings.ACTION_DUR_XLONG = .015F;
        aiServer = new AiServer();
    }

    @Override
    public void receivePostUpdate() {
        if (battleAiController == null && shouldStartAiFromServer) {
            shouldStartAiFromServer = false;
            LudicrousSpeedMod.controller = battleAiController = new BattleAiController(saveState, requestedTurnNum);
        }
    }

    @Override
    public void receivePreUpdate() {
        if (battleAiController == null && shouldStartAiFromServer) {
            shouldStartAiFromServer = false;
            battleAiController = new BattleAiController(saveState, requestedTurnNum);
            LudicrousSpeedMod.controller = battleAiController;
        }
    }

    @Override
    public void receiveEditRelics() {
        // Skipping the card seems to kick the player back to character select (the main menu?)
        // for some reason.
        BaseMod.removeRelic(new TinyHouse());
    }
}
