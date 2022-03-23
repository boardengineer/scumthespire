package autoplay.battleaimod.client;

import autoplay.battleaimod.client.battleai.CommandRunnerController;

import autoplay.battleaimod.client.networking.AiClient;
import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.TopPanelItem;
import basemod.interfaces.*;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.evacipated.cardcrawl.modthespire.ui.ModSelectWindow;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.colorless.Forethought;
import com.megacrit.cardcrawl.cards.purple.Weave;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.relics.*;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.ThoughtBubble;
import ludicrousspeed.LudicrousSpeedMod;
import ludicrousspeed.simulator.commands.GridSelectConfrimCommand;
import ludicrousspeed.simulator.commands.HandSelectCommand;
import ludicrousspeed.simulator.commands.HandSelectConfirmCommand;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import savestate.PotionState;
import savestate.fastobjects.ScreenShakeFast;

import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

@SpireInitializer
public class BattleAiClientMod implements PostInitializeSubscriber, PostUpdateSubscriber, OnStartBattleSubscriber, PreUpdateSubscriber, EditRelicsSubscriber {
    public final static long MESSAGE_TIME_MILLIS = 1500L;

    public static String steveMessage = null;

    public static boolean forceStep = false;
    public static AiClient aiClient = null;

    public static CommandRunnerController rerunController = null;

    public static boolean shouldStartClient = false;

    public BattleAiClientMod() {
        BaseMod.subscribe(this);
        BaseMod.subscribe(new LudicrousSpeedMod());

        // Shut off the MTS console window, It increasingly slows things down
        ModSelectWindow window = ReflectionHacks.getPrivateStatic(Loader.class, "ex");
        window.removeAll();
        window.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        window.setVisible(false);
        Loader.closeWindow();
        CardCrawlGame.screenShake = new ScreenShakeFast();
    }

    public static void initialize() {
        BattleAiClientMod mod = new BattleAiClientMod();
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


        Configurator.setRootLevel(Level.ERROR);
        Settings.MASTER_VOLUME = .7F;
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
    }

    private void setUpOptionsMenu() {
        BaseMod.addTopPanelItem(new StartAiClientTopPanel());
//        BaseMod.addTopPanelItem(new TryButtonPanel());
//        BaseMod.addTopPanelItem(new TryButtonPanel2());
//        BaseMod.addTopPanelItem(new TryButtonPanel3());
    }

    public class StartAiClientTopPanel extends TopPanelItem {
        public static final String ID = "battleaimod:startclient";

        public StartAiClientTopPanel() {
            super(new Texture("img/StartSteve.png"), ID);
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

                        if (BattleAiClientMod.aiClient == null) {
                            try {
                                BattleAiClientMod.aiClient = new AiClient();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        isDone = true;

                        if (BattleAiClientMod.aiClient != null) {
                            BattleAiClientMod.aiClient.sendState();
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
