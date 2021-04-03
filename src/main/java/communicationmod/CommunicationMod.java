package communicationmod;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.TopPanelItem;
import basemod.interfaces.PostDungeonUpdateSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostUpdateSubscriber;
import basemod.interfaces.PreUpdateSubscriber;
import battleai.BattleAiController;
import battleai.EndCommand;
import battleai.StateNode;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.IntentFlashAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.animations.SetAnimationAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.*;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToDiscardEffect;
import fastobjects.ScreenShakeFast;
import fastobjects.actions.*;
import savestate.SaveState;
import skrelpoid.superfastmode.SuperFastMode;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpireInitializer
public class CommunicationMod implements PostInitializeSubscriber, PostUpdateSubscriber, PostDungeonUpdateSubscriber, PreUpdateSubscriber {
    public static boolean mustSendGameState = false;
    public static boolean readyForUpdate;
    public static BattleAiController battleAiController = null;
    private static ShowPNG showPNG;
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

    private void sendGameState() {
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

    public void receivePreUpdate() {
        makeGameVeryFast();
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
        if (readyForUpdate) {
            readyForUpdate = false;
            sendGameState();
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

//        BaseMod.addTopPanelItem(new SaveStateTopPanel());
//        BaseMod.addTopPanelItem(new LoadStateTopPanel());
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
            } else if (effect instanceof FastCardObtainEffect) {
                // don't remove card obtain effects of they get skipped
            }else {
                topLevelEffects.remove();
            }
        }

        Iterator<AbstractGameEffect> effectIterator = AbstractDungeon.effectList.iterator();
        while(effectIterator.hasNext()) {
            AbstractGameEffect effect = effectIterator.next();
            if(!(effect instanceof FastCardObtainEffect || effect instanceof ShowCardAndAddToDiscardEffect)) {
                effectIterator.remove();
            }
        }

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
            } else if (action instanceof ShowMoveNameAction) {
                actions.remove(i);
                i--;
            } else if (action instanceof SetAnimationAction) {
                actions.remove(i);
                i--;
            } else if (action instanceof VFXAction) {
                actions.remove(i);
                i--;
            } else if (action instanceof GainBlockAction) {
//                actions.remove(i);
//                i--;
            } else if (action instanceof RollMoveAction) {
                AbstractMonster monster = ReflectionHacks
                        .getPrivate(action, RollMoveAction.class, "monster");
                actions.remove(i);
                actions.add(i, new RollMoveActionFast(monster));
            }
        }
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
