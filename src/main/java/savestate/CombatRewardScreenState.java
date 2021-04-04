package savestate;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;

import java.util.ArrayList;

// TODO: make encodersss
public class CombatRewardScreenState {
    private final CombatRewardScreen combatRewardScreen;
    private final ArrayList<RewardItem> rewards;
    private final ArrayList<AbstractGameEffect> effects;
    private final boolean hasTakenAll;

    public CombatRewardScreenState(CombatRewardScreen combatRewardScreen) {
        this.combatRewardScreen = combatRewardScreen;

        this.rewards = (ArrayList<RewardItem>) combatRewardScreen.rewards.clone();
        this.effects = (ArrayList<AbstractGameEffect>) combatRewardScreen.effects.clone();
        this.hasTakenAll = combatRewardScreen.hasTakenAll;
    }

    public CombatRewardScreen loadCombatRewardScreen() {
        combatRewardScreen.rewards = (ArrayList<RewardItem>) this.rewards.clone();
        combatRewardScreen.effects = (ArrayList<AbstractGameEffect>) this.effects.clone();
        combatRewardScreen.hasTakenAll = this.hasTakenAll;

        combatRewardScreen.clear();
        combatRewardScreen.update();
        AbstractDungeon.overlayMenu.proceedButton.hide();

        AbstractDungeon.dynamicBanner.hide();

        if (AbstractDungeon.fadeColor.a == 1.0F) {
            AbstractDungeon.fadeIn();
        }

        AbstractDungeon.overlayMenu.hideBlackScreen();
        AbstractDungeon.overlayMenu.showCombatPanels();
        AbstractDungeon.closeCurrentScreen();

        return combatRewardScreen;
    }
}
