package battleaimod.savestate;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

// TODO: make a real version of this if we want a state loader, for now this just closes the
// reward screen to get back to the battle
public class CombatRewardScreenState {
    public static void loadCombatRewardScreen() {
        AbstractDungeon.overlayMenu.proceedButton.hide();

        AbstractDungeon.dynamicBanner.hide();

        if (AbstractDungeon.fadeColor.a == 1.0F) {
            AbstractDungeon.fadeIn();
        }

        AbstractDungeon.overlayMenu.hideBlackScreen();
        AbstractDungeon.overlayMenu.showCombatPanels();
        AbstractDungeon.closeCurrentScreen();
    }
}
