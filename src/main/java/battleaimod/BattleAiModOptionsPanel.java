package battleaimod;

import basemod.ModLabel;
import basemod.ModPanel;
import battleaimod.networking.BattleClientController;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.options.DropdownMenuListener;

import java.util.Arrays;

public class BattleAiModOptionsPanel extends ModPanel implements DropdownMenuListener {
    public static DropdownMenu characters;

    public BattleAiModOptionsPanel() {
        ModLabel helloWorldLabel = new ModLabel(
                "", 350, 600, Settings.CREAM_COLOR, FontHelper.charDescFont,
                this, modLabel -> {
            modLabel.text = "hello world";
        });
        this.addUIElement(helloWorldLabel);

        String[] values = Arrays.stream(BattleClientController.ControllerMode.values())
                                .map(BattleClientController.ControllerMode::toString)
                                .toArray(String[]::new);
        characters = new DropdownMenu(this, values, FontHelper.tipBodyFont, Settings.CREAM_COLOR);
        characters.setSelectedIndex(BattleAiMod.battleClientControllerMode.ordinal());
    }

    @Override
    public void update() {
        super.update();
        characters.update();
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);

        characters.render(sb, 500, 500);
    }

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {
        if (dropdownMenu == characters) {
            BattleAiMod.battleClientControllerMode = BattleClientController.ControllerMode
                    .valueOf(s);
            BattleClientController.saveMode(BattleAiMod.battleClientControllerMode);
        }
    }
}