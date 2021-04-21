package battleaimod.patches;

import basemod.ReflectionHacks;
import battleaimod.BattleAiMod;
import battleaimod.battleai.BattleAiController;
import battleaimod.fastobjects.actions.DrawCardActionFast;
import battleaimod.fastobjects.actions.RollMoveActionFast;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.IntentFlashAction;
import com.megacrit.cardcrawl.actions.animations.SetAnimationAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.defect.TriggerEndOfTurnOrbsAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.blights.AbstractBlight;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.OverlayMenu;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.daily.mods.Careless;
import com.megacrit.cardcrawl.daily.mods.ControlledChaos;
import com.megacrit.cardcrawl.dungeons.*;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.TipTracker;
import com.megacrit.cardcrawl.helpers.input.DevInputActionSet;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.monsters.MonsterQueueItem;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.BottledFlame;
import com.megacrit.cardcrawl.relics.UnceasingTop;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.vfx.GameSavedEffect;
import com.megacrit.cardcrawl.vfx.ThoughtBubble;
import com.megacrit.cardcrawl.vfx.cardManip.ExhaustCardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import com.megacrit.cardcrawl.vfx.combat.BattleStartEffect;
import com.megacrit.cardcrawl.vfx.combat.StrikeEffect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;

import static battleaimod.patches.MonsterPatch.shouldGoFast;
import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;

public class FastActionsPatch {
    @SpirePatch(
            clz = AbstractDungeon.class,
            paramtypez = {},
            method = "update"
    )
    public static class ForceGameActionsPatch {
        public static void Postfix(AbstractDungeon dungeon) {
            GameActionManager actionManager = AbstractDungeon.actionManager;
            if (shouldGoFast()) {
                if (actionManager.phase == GameActionManager.Phase.EXECUTING_ACTIONS || !actionManager.monsterQueue
                        .isEmpty() || shouldStepAiController()) {
                    while (shouldWaitOnActions(actionManager) || shouldStepAiController()) {

                        AbstractDungeon.topLevelEffects.clear();
                        AbstractDungeon.effectList.clear();
                        // TODO this is going to have consequences
                        actionManager.cardsPlayedThisCombat.clear();


                        long startTime = System.currentTimeMillis();
                        if (shouldWaitOnActions(actionManager)) {
                            if (actionManager.currentAction instanceof RollMoveAction) {
                                AbstractMonster monster = ReflectionHacks
                                        .getPrivate(actionManager.currentAction, RollMoveAction.class, "monster");
                                actionManager.currentAction = new RollMoveActionFast(monster);
                            } else if (actionManager.currentAction instanceof DrawCardAction) {
                                actionManager.currentAction = new DrawCardActionFast(AbstractDungeon.player, actionManager.currentAction.amount);
                            } else if (actionManager.currentAction instanceof SetAnimationAction) {
                                actionManager.currentAction = null;
                            } else if (actionManager.currentAction instanceof ShowMoveNameAction) {
                                actionManager.currentAction = null;
                            }
                            if (actionManager.currentAction != null) {
                                Class actionClass = actionManager.currentAction.getClass();
                                actionManager.currentAction.update();
                                if (BattleAiMod.battleAiController != null) {
                                    long timeThisAction = (System
                                            .currentTimeMillis() - startTime);
                                    BattleAiMod.battleAiController.actionTime += timeThisAction;
                                    HashMap<Class, Long> actionClassTimes = BattleAiMod.battleAiController.actionClassTimes;
                                    if (actionClassTimes != null) {
                                        if (actionClassTimes.containsKey(actionClass)) {
                                            actionClassTimes.put(actionClass, actionClassTimes
                                                    .get(actionClass) + timeThisAction);
                                        } else {
                                            actionClassTimes.put(actionClass, timeThisAction);
                                        }
                                    }
                                }
                            }
                        } else if (shouldStepAiController()) {
                            BattleAiMod.readyForUpdate = false;
                            BattleAiMod.battleAiController.step();
                            BattleAiMod.battleAiController.stepTime += (System
                                    .currentTimeMillis() - startTime);
                        }

//                        AbstractDungeon.getCurrRoom().update();
                        roomUpdate();

                        if (AbstractDungeon.player.currentHealth <= 0) {
                            if (actionManager.currentAction instanceof DamageAction) {
                                actionManager.update();
                            }
                            break;
                        }
                    }

                    System.err.println("exiting loop");
                }
            }
        }
    }

    @SpirePatch(
            clz = RemoveSpecificPowerAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FastRemovePowerPatch {
        public static void Prefix(RemoveSpecificPowerAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .1F);
            }
        }

        public static void Postfix(RemoveSpecificPowerAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = ApplyPowerAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FastApplyPowerActionPatch {
        public static void Prefix(ApplyPowerAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .1F);
            }
        }

        public static void Postfix(ApplyPowerAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = AbstractRoom.class,
            paramtypez = {},
            method = "endBattle"
    )
    public static class EndBattlePatch {
        public static void Postfix(AbstractRoom _instance) {
            if (shouldGoFast()) {
                BattleAiMod.readyForUpdate = true;
            }
        }
    }

    @SpirePatch(
            clz = MakeTempCardInDiscardAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class MakeTempCardsFastPatch {
        public static void Prefix(MakeTempCardInDiscardAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .001F);

                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "startDuration", .001F);
            }
        }

        public static void Postfix(MakeTempCardInDiscardAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = GainBlockAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class GainBlockActionFastPatch {
        public static void Prefix(GainBlockAction _instance) {
            if (shouldGoFast()) {
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .001F);

                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "startDuration", .001F);
            }
        }

        public static void Postfix(GainBlockAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    private static boolean shouldStepAiController() {
        return BattleAiMod.battleAiController != null && !BattleAiMod.battleAiController.isDone && BattleAiMod.readyForUpdate && actionManager.phase == GameActionManager.Phase.WAITING_ON_USER;
    }

    private static boolean shouldWaitOnActions(GameActionManager actionManager) {
        return actionManager.currentAction != null && !actionManager.currentAction.isDone || !actionManager.monsterQueue
                .isEmpty() || !actionManager.actions.isEmpty();
    }


    @SpirePatch(
            clz = MonsterRoom.class,
            paramtypez = {},
            method = "onPlayerEntry"
    )
    public static class SpyOnMonsterRoomPatch {
        public static void Prefix(MonsterRoom _instance) {
            System.err.println("Starting fight " + AbstractDungeon.monsterList.get(0));
            BattleAiController.currentEncounter = AbstractDungeon.monsterList.get(0);
        }


    }

    @SpirePatch(
            clz = MonsterRoomElite.class,
            paramtypez = {},
            method = "onPlayerEntry"
    )
    public static class SpyOnEliteMonsterRoomPatch {
        public static void Prefix(MonsterRoomElite _instance) {
            System.err.println("Starting fight " + AbstractDungeon.eliteMonsterList.get(0));
            BattleAiController.currentEncounter = AbstractDungeon.eliteMonsterList.get(0);
        }
    }

    @SpirePatch(
            clz = MonsterRoomBoss.class,
            paramtypez = {},
            method = "onPlayerEntry"
    )
    public static class SpyOnBossMonsterRoomPatch {
        public static void Prefix(MonsterRoomBoss _instance) {
            System.err.println("Starting fight " + AbstractDungeon.bossList.get(0));
            BattleAiController.currentEncounter = AbstractDungeon.bossList.get(0);
        }
    }

    // TODO this is a hack to fix an NPE
    @SpirePatch(
            clz = BottledFlame.class,
            paramtypez = {},
            method = "setDescriptionAfterLoading"
    )
    public static class FixDescriptionNPE {
        public static void Replace(BottledFlame _instance) {

        }
    }

    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {},
            method = "draw"
    )
    public static class NoSoundDrawPatch {
        public static void Replace(AbstractPlayer _instance) {
            if (_instance.hand.size() == 10) {
                _instance.createHandIsFullDialog();
            } else {
                _instance.draw(1);
                _instance.onCardDrawOrDiscard();
            }
        }
    }

    @SpirePatch(
            clz = DeathScreen.class,
            paramtypez = {MonsterGroup.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class DisableDeathScreenpatch {
        public static SpireReturn Prefix(DeathScreen _instance, MonsterGroup monsterGroup) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = StrikeEffect.class,
            paramtypez = {AbstractCreature.class, float.class, float.class, int.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class TooManyLinesPatch {
        public static SpireReturn Prefix(StrikeEffect _instance, AbstractCreature target, float x, float y, int number) {
            if (shouldGoFast()) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = OverlayMenu.class,
            paramtypez = {},
            method = "showCombatPanels"
    )
    public static class TooMdddanyLinesPatch {
        public static SpireReturn Prefix(OverlayMenu _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = DamageAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class ForceDamageActionPatch {
        public static void Prefix(DamageAction _instance) {
            _instance.isDone = true;
        }
    }

    // THIS IS VOODOO, DON'T TOUCH IT
    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {int.class},
            method = "draw"
    )
    public static class NoSoundDrawPatch2 {
        private static final Logger logger = LogManager.getLogger(AbstractPlayer.class.getName());

        public static void Replace(AbstractPlayer _instance, int numCards) {
            for (int i = 0; i < numCards; ++i) {
                if (_instance.drawPile.isEmpty()) {
                    logger.info("ERROR: How did this happen? No cards in draw pile?? Player.java");
                } else {
                    AbstractCard c = _instance.drawPile.getTopCard();
                    c.current_x = CardGroup.DRAW_PILE_X;
                    c.current_y = CardGroup.DRAW_PILE_Y;
                    c.setAngle(0.0F, true);
                    c.lighten(false);
                    c.drawScale = 0.12F;
                    c.targetDrawScale = 0.75F;
                    c.triggerWhenDrawn();
                    _instance.hand.addToHand(c);
                    _instance.drawPile.removeTopCard();
                    Iterator var4 = _instance.powers.iterator();

                    while (var4.hasNext()) {
                        AbstractPower p = (AbstractPower) var4.next();
                        p.onCardDraw(c);
                    }

                    var4 = _instance.relics.iterator();

                    while (var4.hasNext()) {
                        AbstractRelic r = (AbstractRelic) var4.next();
                        r.onCardDraw(c);
                    }
                }
            }
        }
    }

    static void roomUpdate() {
        AbstractDungeon.getCurrRoom().monsters.update();
        if (!(AbstractDungeon.getCurrRoom().waitTimer > 0.0F)) {

            if (Settings.isDebug && DevInputActionSet.drawCard.isJustPressed()) {
                AbstractDungeon.actionManager
                        .addToTop(new DrawCardAction(AbstractDungeon.player, 1));
            }


            if (!AbstractDungeon.isScreenUp) {
//                System.err.println("updating room " + actionManager.cardsPlayedThisCombat
//                        .size() + " " + actionManager.cardsPlayedThisTurn.size());

                ActionManageUpdate();

                long preUpdateTime = System.currentTimeMillis();

                if (!AbstractDungeon.getCurrRoom().monsters
                        .areMonstersBasicallyDead() && AbstractDungeon.player.currentHealth > 0) {
                    AbstractDungeon.player.updateInput();
                }


                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController.updateTime += (System
                            .currentTimeMillis() - preUpdateTime);
                }
            }


            if (!AbstractDungeon.screen.equals(AbstractDungeon.CurrentScreen.HAND_SELECT)) {
                AbstractDungeon.player.combatUpdate();
            }

            if (AbstractDungeon.player.isEndingTurn) {
                AbstractDungeon.getCurrRoom().endTurn();
            }


        } else {
            if (AbstractDungeon.actionManager.currentAction == null && AbstractDungeon.actionManager
                    .isEmpty()) {
                AbstractDungeon.getCurrRoom().waitTimer -= Gdx.graphics.getDeltaTime();
            } else {
                AbstractDungeon.actionManager.update();
            }

            if (AbstractDungeon.getCurrRoom().waitTimer <= 0.0F) {
                AbstractDungeon.actionManager.turnHasEnded = true;
                if (!AbstractDungeon.isScreenUp) {
                    AbstractDungeon.topLevelEffects.add(new BattleStartEffect(false));
                }

                AbstractDungeon.actionManager
                        .addToBottom(new GainEnergyAndEnableControlsAction(AbstractDungeon.player.energy.energyMaster));
                AbstractDungeon.player.applyStartOfCombatPreDrawLogic();
                AbstractDungeon.actionManager
                        .addToBottom(new DrawCardAction(AbstractDungeon.player, AbstractDungeon.player.gameHandSize));
                AbstractDungeon.actionManager.addToBottom(new EnableEndTurnButtonAction());
                AbstractDungeon.overlayMenu.showCombatPanels();
                AbstractDungeon.player.applyStartOfCombatLogic();
                if (ModHelper.isModEnabled("Careless")) {
                    Careless.modAction();
                }

                if (ModHelper.isModEnabled("ControlledChaos")) {
                    ControlledChaos.modAction();
                }

                AbstractDungeon.getCurrRoom().skipMonsterTurn = false;
                AbstractDungeon.player.applyStartOfTurnRelics();
                AbstractDungeon.player.applyStartOfTurnPostDrawRelics();
                AbstractDungeon.player.applyStartOfTurnCards();
                AbstractDungeon.player.applyStartOfTurnPowers();
                AbstractDungeon.player.applyStartOfTurnOrbs();
                AbstractDungeon.actionManager.useNextCombatActions();
            }
        }

        if (AbstractDungeon.getCurrRoom().isBattleOver && AbstractDungeon.actionManager.actions
                .isEmpty()) {

            AbstractDungeon.getCurrRoom().skipMonsterTurn = false;
            float endBattleTimer = -1.F;
            if (endBattleTimer < 0.0F) {
                AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
                if (!(AbstractDungeon
                        .getCurrRoom() instanceof MonsterRoomBoss) || !(CardCrawlGame.dungeon instanceof TheBeyond) || Settings.isEndless) {
                    CardCrawlGame.sound.play("VICTORY");
                }

                endBattleTimer = 0.0F;
                int card_seed_before_roll;
                if (AbstractDungeon
                        .getCurrRoom() instanceof MonsterRoomBoss && !AbstractDungeon.loading_post_combat) {
                    if (!CardCrawlGame.loadingSave) {
                        if (Settings.isDailyRun) {
                            AbstractDungeon.getCurrRoom().addGoldToRewards(100);
                        } else {
                            card_seed_before_roll = 100 + AbstractDungeon.miscRng.random(-5, 5);
                            if (AbstractDungeon.ascensionLevel >= 13) {
                                AbstractDungeon.getCurrRoom().addGoldToRewards(MathUtils
                                        .round((float) card_seed_before_roll * 0.75F));
                            } else {
                                AbstractDungeon.getCurrRoom()
                                               .addGoldToRewards(card_seed_before_roll);
                            }
                        }
                    }

                    if (ModHelper.isModEnabled("Cursed Run")) {
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(AbstractDungeon
                                .returnRandomCurse(), (float) Settings.WIDTH / 2.0F, (float) Settings.HEIGHT / 2.0F));
                    }
                } else if (AbstractDungeon
                        .getCurrRoom() instanceof MonsterRoomElite && !AbstractDungeon.loading_post_combat) {
                    if (CardCrawlGame.dungeon instanceof Exordium) {
                        ++CardCrawlGame.elites1Slain;
                    } else if (CardCrawlGame.dungeon instanceof TheCity) {
                        ++CardCrawlGame.elites2Slain;
                    } else if (CardCrawlGame.dungeon instanceof TheBeyond) {
                        ++CardCrawlGame.elites3Slain;
                    } else {
                        ++CardCrawlGame.elitesModdedSlain;
                    }

                    if (!CardCrawlGame.loadingSave) {
                        if (Settings.isDailyRun) {
                            AbstractDungeon.getCurrRoom().addGoldToRewards(30);
                        } else {
                            AbstractDungeon.getCurrRoom()
                                           .addGoldToRewards(AbstractDungeon.treasureRng
                                                   .random(25, 35));
                        }
                    }
                } else if (AbstractDungeon.getCurrRoom() instanceof MonsterRoom && !AbstractDungeon
                        .getMonsters().haveMonstersEscaped()) {
                    ++CardCrawlGame.monstersSlain;
                    if (Settings.isDailyRun) {
                        AbstractDungeon.getCurrRoom().addGoldToRewards(15);
                    } else {
                        AbstractDungeon.getCurrRoom().addGoldToRewards(AbstractDungeon.treasureRng
                                .random(10, 20));
                    }
                }

                if (!(AbstractDungeon
                        .getCurrRoom() instanceof MonsterRoomBoss) || !(CardCrawlGame.dungeon instanceof TheBeyond) && !(CardCrawlGame.dungeon instanceof TheEnding) || Settings.isEndless) {
                    if (!AbstractDungeon.loading_post_combat) {
                        AbstractDungeon.getCurrRoom().dropReward();
                        AbstractDungeon.getCurrRoom().addPotionToRewards();
                    }

                    card_seed_before_roll = AbstractDungeon.cardRng.counter;
                    int card_randomizer_before_roll = AbstractDungeon.cardBlizzRandomizer;
                    if (AbstractDungeon.getCurrRoom().rewardAllowed) {
                        if (AbstractDungeon.getCurrRoom().mugged) {
                        } else if (AbstractDungeon.getCurrRoom().smoked) {
                        } else {
                            AbstractDungeon.combatRewardScreen.open();
                        }

                        if (!CardCrawlGame.loadingSave && !AbstractDungeon.loading_post_combat) {
                            SaveFile saveFile = new SaveFile(SaveFile.SaveType.POST_COMBAT);
                            saveFile.card_seed_count = card_seed_before_roll;
                            saveFile.card_random_seed_randomizer = card_randomizer_before_roll;
                            if (AbstractDungeon.getCurrRoom().combatEvent) {
                                --saveFile.event_seed_count;
                            }

                            SaveAndContinue.save(saveFile);
                            AbstractDungeon.effectList.add(new GameSavedEffect());
                        } else {
                            CardCrawlGame.loadingSave = false;
                        }

                        AbstractDungeon.loading_post_combat = false;
                    }
                }
            }
        }

        AbstractDungeon.getCurrRoom().monsters.updateAnimations();
    }

    public static void ActionManageUpdate() {
        switch(AbstractDungeon.actionManager.phase) {
            case WAITING_ON_USER:
                ActionManagerNextAction();
                break;
            case EXECUTING_ACTIONS:
                if (AbstractDungeon.actionManager.currentAction != null && !AbstractDungeon.actionManager.currentAction.isDone) {
                    AbstractDungeon.actionManager.currentAction.update();
                } else {
                    AbstractDungeon.actionManager.previousAction = AbstractDungeon.actionManager.currentAction;
                    AbstractDungeon.actionManager.currentAction = null;
                    ActionManagerNextAction();
                    if (AbstractDungeon.actionManager.currentAction == null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && !actionManager.usingCard) {
                        AbstractDungeon.actionManager.phase = GameActionManager.Phase.WAITING_ON_USER;
                        AbstractDungeon.player.hand.refreshHandLayout();
                        AbstractDungeon.actionManager.hasControl = false;
                    }

                    AbstractDungeon.actionManager.usingCard = false;
                }
                break;
            default:
        }

    }


    public static void ActionManagerNextAction() {
        if (!AbstractDungeon.actionManager.actions.isEmpty()) {
            AbstractDungeon.actionManager.currentAction = (AbstractGameAction)AbstractDungeon.actionManager.actions.remove(0);
            AbstractDungeon.actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;
            AbstractDungeon.actionManager.hasControl = true;
        } else if (!AbstractDungeon.actionManager.preTurnActions.isEmpty()) {
            AbstractDungeon.actionManager.currentAction = (AbstractGameAction)AbstractDungeon.actionManager.preTurnActions.remove(0);
            AbstractDungeon.actionManager.phase = GameActionManager.Phase.EXECUTING_ACTIONS;
            AbstractDungeon.actionManager.hasControl = true;
        } else if (!AbstractDungeon.actionManager.cardQueue.isEmpty()) {
            AbstractDungeon.actionManager.usingCard = true;
            AbstractCard c = ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card;
            if (c == null) {
                callEndOfTurnActions();
            } else if (c.equals(AbstractDungeon.actionManager.lastCard)) {
                AbstractDungeon.actionManager.lastCard = null;
            }

            if (AbstractDungeon.actionManager.cardQueue.size() == 1 && ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).isEndTurnAutoPlay) {
                AbstractRelic top = AbstractDungeon.player.getRelic("Unceasing Top");
                if (top != null) {
                    ((UnceasingTop)top).disableUntilTurnEnds();
                }
            }

            boolean canPlayCard = false;
            if (c != null) {
                c.isInAutoplay = ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).autoplayCard;
            }

            if (c != null && ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).randomTarget) {
                ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).monster = AbstractDungeon.getMonsters().getRandomMonster((AbstractMonster)null, true, AbstractDungeon.cardRandomRng);
            }

            Iterator i;
            AbstractCard card;
            if (((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card == null || !c.canUse(AbstractDungeon.player, ((CardQueueItem)actionManager.cardQueue.get(0)).monster) && !((CardQueueItem)actionManager.cardQueue.get(0)).card.dontTriggerOnUseCard) {
                i = AbstractDungeon.player.limbo.group.iterator();

                while(i.hasNext()) {
                    card = (AbstractCard)i.next();
                    if (card == c) {
                        c.fadingOut = true;
                        AbstractDungeon.effectList.add(new ExhaustCardEffect(c));
                        i.remove();
                    }
                }

                if (c != null) {
                    AbstractDungeon.effectList.add(new ThoughtBubble(AbstractDungeon.player.dialogX, AbstractDungeon.player.dialogY, 3.0F, c.cantUseMessage, true));
                }
            } else {
                canPlayCard = true;
                if (c.freeToPlay()) {
                    c.freeToPlayOnce = true;
                }

                ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card.energyOnUse = ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).energyOnUse;
                if (c.isInAutoplay) {
                    ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card.ignoreEnergyOnUse = true;
                } else {
                    ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card.ignoreEnergyOnUse = ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).ignoreEnergyTotal;
                }

                if (!((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card.dontTriggerOnUseCard) {
                    i = AbstractDungeon.player.powers.iterator();

                    while(i.hasNext()) {
                        AbstractPower p = (AbstractPower)i.next();
                        p.onPlayCard(((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card, ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).monster);
                    }

                    i = AbstractDungeon.getMonsters().monsters.iterator();

                    while(i.hasNext()) {
                        AbstractMonster m = (AbstractMonster)i.next();
                        Iterator var5 = m.powers.iterator();

                        while(var5.hasNext()) {
                            AbstractPower p = (AbstractPower)var5.next();
                            p.onPlayCard(((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card, ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).monster);
                        }
                    }

                    i = AbstractDungeon.player.relics.iterator();

                    while(i.hasNext()) {
                        AbstractRelic r = (AbstractRelic)i.next();
                        r.onPlayCard(((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card, ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).monster);
                    }

                    AbstractDungeon.player.stance.onPlayCard(((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card);
                    i = AbstractDungeon.player.blights.iterator();

                    while(i.hasNext()) {
                        AbstractBlight b = (AbstractBlight)i.next();
                        b.onPlayCard(((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card, ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).monster);
                    }

                    i = AbstractDungeon.player.hand.group.iterator();

                    while(i.hasNext()) {
                        card = (AbstractCard)i.next();
                        card.onPlayCard(((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card, ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).monster);
                    }

                    i = AbstractDungeon.player.discardPile.group.iterator();

                    while(i.hasNext()) {
                        card = (AbstractCard)i.next();
                        card.onPlayCard(((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card, ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).monster);
                    }

                    i = AbstractDungeon.player.drawPile.group.iterator();

                    while(i.hasNext()) {
                        card = (AbstractCard)i.next();
                        card.onPlayCard(((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card, ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).monster);
                    }

                    ++AbstractDungeon.player.cardsPlayedThisTurn;
                    AbstractDungeon.actionManager.cardsPlayedThisTurn.add(((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card);
                    AbstractDungeon.actionManager.cardsPlayedThisCombat.add(((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card);
                }

                if (AbstractDungeon.actionManager.cardsPlayedThisTurn.size() == 25) {
                    UnlockTracker.unlockAchievement("INFINITY");
                }

                if (AbstractDungeon.actionManager.cardsPlayedThisTurn.size() >= 20 && !CardCrawlGame.combo) {
                    CardCrawlGame.combo = true;
                }

                if (((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card instanceof Shiv) {
                    int shivCount = 0;
                    Iterator var15 = AbstractDungeon.actionManager.cardsPlayedThisTurn.iterator();

                    while(var15.hasNext()) {
                        AbstractCard card2 = (AbstractCard)var15.next();
                        if (card2 instanceof Shiv) {
                            ++shivCount;
                            if (shivCount == 10) {
                                UnlockTracker.unlockAchievement("NINJA");
                                break;
                            }
                        }
                    }
                }

                if (((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card != null) {
                    if (((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card.target != AbstractCard.CardTarget.ENEMY || ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).monster != null && !((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).monster.isDeadOrEscaped()) {
                        AbstractDungeon.player.useCard(((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card, ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).monster, ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).energyOnUse);
                    } else {
                        i = AbstractDungeon.player.limbo.group.iterator();

                        while(i.hasNext()) {
                            card = (AbstractCard)i.next();
                            if (card == ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card) {
                                ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card.fadingOut = true;
                                AbstractDungeon.effectList.add(new ExhaustCardEffect(((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card));
                                i.remove();
                            }
                        }

                        if (((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).monster == null) {
                            ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card.drawScale = ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card.targetDrawScale;
                            ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card.angle = ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card.targetAngle;
                            ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card.current_x = ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card.target_x;
                            ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card.current_y = ((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card.target_y;
                            AbstractDungeon.effectList.add(new ExhaustCardEffect(((CardQueueItem)AbstractDungeon.actionManager.cardQueue.get(0)).card));
                        }
                    }
                }
            }

            AbstractDungeon.actionManager.cardQueue.remove(0);
            if (!canPlayCard && c != null && c.isInAutoplay) {
                c.dontTriggerOnUseCard = true;
                AbstractDungeon.actionManager.addToBottom(new UseCardAction(c));
            }
        } else if (!AbstractDungeon.actionManager.monsterAttacksQueued) {
            AbstractDungeon.actionManager.monsterAttacksQueued = true;
            if (!AbstractDungeon.getCurrRoom().skipMonsterTurn) {
                AbstractDungeon.getCurrRoom().monsters.queueMonsters();
            }
        } else if (!AbstractDungeon.actionManager.monsterQueue.isEmpty()) {
            AbstractMonster m = ((MonsterQueueItem)AbstractDungeon.actionManager.monsterQueue.get(0)).monster;
            if (!m.isDeadOrEscaped() || m.halfDead) {
                if (m.intent != AbstractMonster.Intent.NONE) {
                    AbstractDungeon.actionManager.addToBottom(new ShowMoveNameAction(m));
                    AbstractDungeon.actionManager.addToBottom(new IntentFlashAction(m));
                }

                if (!(Boolean) TipTracker.tips.get("INTENT_TIP") && AbstractDungeon.player.currentBlock == 0 && (m.intent == AbstractMonster.Intent.ATTACK || m.intent == AbstractMonster.Intent.ATTACK_DEBUFF || m.intent == AbstractMonster.Intent.ATTACK_BUFF || m.intent == AbstractMonster.Intent.ATTACK_DEFEND)) {
                    if (AbstractDungeon.floorNum <= 5) {
                        ++TipTracker.blockCounter;
                    } else {
                        TipTracker.neverShowAgain("INTENT_TIP");
                    }
                }

                m.takeTurn();
                m.applyTurnPowers();
            }

            AbstractDungeon.actionManager.monsterQueue.remove(0);
            if (AbstractDungeon.actionManager.monsterQueue.isEmpty()) {
                AbstractDungeon.actionManager.addToBottom(new WaitAction(1.5F));
            }
        } else if (AbstractDungeon.actionManager.turnHasEnded && !AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
            if (!AbstractDungeon.getCurrRoom().skipMonsterTurn) {
                AbstractDungeon.getCurrRoom().monsters.applyEndOfTurnPowers();
            }

            AbstractDungeon.player.cardsPlayedThisTurn = 0;
            AbstractDungeon.actionManager.orbsChanneledThisTurn.clear();
            if (ModHelper.isModEnabled("Careless")) {
                Careless.modAction();
            }

            if (ModHelper.isModEnabled("ControlledChaos")) {
                ControlledChaos.modAction();
                AbstractDungeon.player.hand.applyPowers();
            }

            AbstractDungeon.player.applyStartOfTurnRelics();
            AbstractDungeon.player.applyStartOfTurnPreDrawCards();
            AbstractDungeon.player.applyStartOfTurnCards();
            AbstractDungeon.player.applyStartOfTurnPowers();
            AbstractDungeon.player.applyStartOfTurnOrbs();
            ++AbstractDungeon.actionManager.turn;
            AbstractDungeon.getCurrRoom().skipMonsterTurn = false;
            AbstractDungeon.actionManager.turnHasEnded = false;
            AbstractDungeon.actionManager.totalDiscardedThisTurn = 0;
            AbstractDungeon.actionManager.cardsPlayedThisTurn.clear();
            AbstractDungeon.actionManager.damageReceivedThisTurn = 0;
            if (!AbstractDungeon.player.hasPower("Barricade") && !AbstractDungeon.player.hasPower("Blur")) {
                if (!AbstractDungeon.player.hasRelic("Calipers")) {
                    AbstractDungeon.player.loseBlock();
                } else {
                    AbstractDungeon.player.loseBlock(15);
                }
            }

            if (!AbstractDungeon.getCurrRoom().isBattleOver) {
                AbstractDungeon.actionManager.addToBottom(new DrawCardAction((AbstractCreature)null, AbstractDungeon.player.gameHandSize, true));
                AbstractDungeon.player.applyStartOfTurnPostDrawRelics();
                AbstractDungeon.player.applyStartOfTurnPostDrawPowers();
                AbstractDungeon.actionManager.addToBottom(new EnableEndTurnButtonAction());
            }
        }

    }

    private static void callEndOfTurnActions() {
        AbstractDungeon.getCurrRoom().applyEndOfTurnRelics();
        AbstractDungeon.getCurrRoom().applyEndOfTurnPreCardPowers();
        actionManager.addToBottom(new TriggerEndOfTurnOrbsAction());
        Iterator var1 = AbstractDungeon.player.hand.group.iterator();

        while(var1.hasNext()) {
            AbstractCard c = (AbstractCard)var1.next();
            c.triggerOnEndOfTurnForPlayingCard();
        }

        AbstractDungeon.player.stance.onEndOfTurn();
    }

    @SpirePatch(
            clz = CombatRewardScreen.class,
            paramtypez = {SpriteBatch.class},
            method = "render"
    )
    public static class NoRenderBodyPatch {
        public static SpireReturn Prefix(CombatRewardScreen _instance, SpriteBatch sb) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
