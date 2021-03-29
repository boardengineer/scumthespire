package communicationmod;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.TopPanelItem;
import basemod.interfaces.PostDungeonUpdateSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostUpdateSubscriber;
import basemod.interfaces.PreUpdateSubscriber;
import battleai.BattleAiController;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.IntentFlashAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.CardTrailEffect;
import com.megacrit.cardcrawl.vfx.EnemyTurnEffect;
import com.megacrit.cardcrawl.vfx.PlayerTurnEffect;
import fastobjects.ScreenShakeFast;
import fastobjects.actions.*;
import savestate.SaveState;
import skrelpoid.superfastmode.SuperFastMode;

import java.util.Iterator;
import java.util.List;

@SpireInitializer
public class CommunicationMod implements PostInitializeSubscriber, PostUpdateSubscriber, PostDungeonUpdateSubscriber, PreUpdateSubscriber {
    public static boolean mustSendGameState = false;
    public static boolean readyForUpdate;
    private static BattleAiController battleAiController = null;
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
    }

    public static void initialize() {
        CommunicationMod mod = new CommunicationMod();
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
                    battleAiController = null;
                }
            }
        }
    }

    public void receivePreUpdate() {
        makeGameVeryFast();
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
        BaseMod.addTopPanelItem(new StartAIPanel());
        BaseMod.addTopPanelItem(new StepTopPanel());
    }

    private void makeGameVeryFast() {
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
