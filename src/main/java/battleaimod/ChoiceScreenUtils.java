package battleaimod;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.*;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MenuCancelButton;
import com.megacrit.cardcrawl.screens.select.BossRelicSelectScreen;
import com.megacrit.cardcrawl.ui.buttons.SingingBowlButton;
import com.megacrit.cardcrawl.ui.buttons.SkipCardButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;


public class ChoiceScreenUtils {

    private static final Logger logger = LogManager.getLogger(ChoiceScreenUtils.class.getName());

    public enum ChoiceType {
        EVENT,
        CHEST,
        SHOP_ROOM,
        REST,
        CARD_REWARD,
        COMBAT_REWARD,
        MAP,
        BOSS_REWARD,
        SHOP_SCREEN,
        GRID,
        HAND_SELECT,
        GAME_OVER,
        COMPLETE,
        NONE
    }

    public static ChoiceType getCurrentChoiceType() {
        if (!AbstractDungeon.isScreenUp) {
            if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.EVENT || (AbstractDungeon.getCurrRoom().event != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMPLETE)) {
                return ChoiceType.EVENT;
            } else if (AbstractDungeon.getCurrRoom() instanceof TreasureRoomBoss || AbstractDungeon.getCurrRoom() instanceof TreasureRoom) {
                return ChoiceType.CHEST;
            } else if (AbstractDungeon.getCurrRoom() instanceof ShopRoom) {
                return ChoiceType.SHOP_ROOM;
            } else if (AbstractDungeon.getCurrRoom() instanceof RestRoom) {
                return ChoiceType.REST;
            } else if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMPLETE && AbstractDungeon.actionManager.isEmpty() && !AbstractDungeon.isFadingOut) {
                if (AbstractDungeon.getCurrRoom().event == null || (!(AbstractDungeon.getCurrRoom().event instanceof AbstractImageEvent) && (!AbstractDungeon.getCurrRoom().event.hasFocus))) {
                    return ChoiceType.COMPLETE;
                }
            } else {
                return ChoiceType.NONE;
            }
        }
        AbstractDungeon.CurrentScreen screen = AbstractDungeon.screen;
        switch(screen) {
            case CARD_REWARD:
                return ChoiceType.CARD_REWARD;
            case COMBAT_REWARD:
                return ChoiceType.COMBAT_REWARD;
            case MAP:
                return ChoiceType.MAP;
            case BOSS_REWARD:
                return ChoiceType.BOSS_REWARD;
            case SHOP:
                return ChoiceType.SHOP_SCREEN;
            case GRID:
                return ChoiceType.GRID;
            case HAND_SELECT:
                return ChoiceType.HAND_SELECT;
            case DEATH:
            case VICTORY:
            case UNLOCK:
            case NEOW_UNLOCK:
                return ChoiceType.GAME_OVER;
            default:
                return ChoiceType.NONE;
        }
    }

    public static ArrayList<String> getCurrentChoiceList() {
        ChoiceType choiceType = getCurrentChoiceType();
        ArrayList<String> choices;
        switch (choiceType) {
            case CARD_REWARD:
                choices = getCardRewardScreenChoices();
                break;
            case COMBAT_REWARD:
                choices = getCombatRewardScreenChoices();
                break;
            case BOSS_REWARD:
                choices = getBossRewardScreenChoices();
                break;
            default:
                return new ArrayList<>();
        }
        ArrayList<String> lowerCaseChoices = new ArrayList<>();
        for(String item : choices) {
            lowerCaseChoices.add(item.toLowerCase());
        }
        return lowerCaseChoices;
    }

    private static boolean isCancelButtonAvailable(ChoiceType choiceType) {
        switch (choiceType) {
            case EVENT:
                return false;
            case CHEST:
                return false;
            case SHOP_ROOM:
                return false;
            case REST:
                return false;
            case CARD_REWARD:
                return isCardRewardSkipAvailable();
            case COMBAT_REWARD:
                return false;
            case MAP:
                return AbstractDungeon.dungeonMapScreen.dismissable;
            case BOSS_REWARD:
                return true;
            case SHOP_SCREEN:
                return true;
            case HAND_SELECT:
                return false;
            case GAME_OVER:
                return false;
            case COMPLETE:
                return false;
            default:
                return false;
        }
    }

    public static boolean isCancelButtonAvailable() {
        return isCancelButtonAvailable(getCurrentChoiceType());
    }

    private static String getCancelButtonText(ChoiceType choiceType) {
        switch (choiceType) {
            case CARD_REWARD:
                return "skip";
            case MAP:
                return "return";
            case BOSS_REWARD:
                return "skip";
            case SHOP_SCREEN:
                return "leave";
            case GRID:
                return "cancel";
            default:
                return "cancel";
        }
    }

    public static String getCancelButtonText() {
        return getCancelButtonText(getCurrentChoiceType());
    }

    private static void pressCancelButton(ChoiceType choiceType) {
        switch (choiceType) {
            case CARD_REWARD:
                AbstractDungeon.closeCurrentScreen();
                return;
            case BOSS_REWARD:
                MenuCancelButton button = (MenuCancelButton)ReflectionHacks.getPrivate(AbstractDungeon.bossRelicScreen, BossRelicSelectScreen.class, "cancelButton");
                button.hb.clicked = true;
                return;
        }
    }

    public static void pressCancelButton() {
        pressCancelButton(getCurrentChoiceType());
    }

    private static boolean isConfirmButtonAvailable(ChoiceType choiceType) {
        switch (choiceType) {
            case EVENT:
                return false;
            case CHEST:
                return true;
            case SHOP_ROOM:
                return true;
            case CARD_REWARD:
                return false;
            case COMBAT_REWARD:
                return true;
            case MAP:
                return false;
            case BOSS_REWARD:
                return false;
            case SHOP_SCREEN:
                return false;
            case GAME_OVER:
                return true;
            case COMPLETE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isConfirmButtonAvailable() {
        return isConfirmButtonAvailable(getCurrentChoiceType());
    }

    private static String getConfirmButtonText(ChoiceType choiceType) {
        switch (choiceType) {
            case CHEST:
                return "proceed";
            case SHOP_ROOM:
                return "proceed";
            case REST:
                return "proceed";
            case COMBAT_REWARD:
                return "proceed";
            case GRID:
                return "confirm";
            case HAND_SELECT:
                return "confirm";
            case GAME_OVER:
                return "proceed";
            case COMPLETE:
                return "proceed";
            default:
                return "confirm";
        }
    }

    public static String getConfirmButtonText() {
        return getConfirmButtonText(getCurrentChoiceType());
    }

    public static ArrayList<String> getCardRewardScreenChoices() {
        ArrayList<String> choices = new ArrayList<>();
        for(AbstractCard card : AbstractDungeon.cardRewardScreen.rewardGroup) {
            choices.add(card.name.toLowerCase());
        }
        if(isBowlAvailable()) {
            choices.add("bowl");
        }
        return choices;
    }

    public static boolean isBowlAvailable() {
        SingingBowlButton bowlButton = (SingingBowlButton) ReflectionHacks.getPrivate(AbstractDungeon.cardRewardScreen, CardRewardScreen.class, "bowlButton");
        return !((boolean) ReflectionHacks.getPrivate(bowlButton, SingingBowlButton.class, "isHidden"));
    }

    public static boolean isCardRewardSkipAvailable() {
        SkipCardButton skipButton = (SkipCardButton) ReflectionHacks.getPrivate(AbstractDungeon.cardRewardScreen, CardRewardScreen.class, "skipButton");
        return !((boolean) ReflectionHacks.getPrivate(skipButton, SkipCardButton.class, "isHidden"));
    }

    public static ArrayList<String> getCombatRewardScreenChoices() {
        ArrayList<String> choices = new ArrayList<>();
        for(RewardItem reward : AbstractDungeon.combatRewardScreen.rewards) {
            choices.add(reward.type.name().toLowerCase());
        }
        return choices;
    }

    public static ArrayList<String> getBossRewardScreenChoices() {
        ArrayList<String> choices = new ArrayList<>();
        for(AbstractRelic relic : AbstractDungeon.bossRelicScreen.relics) {
            choices.add(relic.name);
        }
        return choices;
    }
}
