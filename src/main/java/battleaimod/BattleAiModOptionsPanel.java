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
    private static final float LABEL_X_POS = Settings.WIDTH / 5.0F;
    private static final float LABEL_Y_POS = Settings.HEIGHT * 2.0F / 3.0F;
    public static DropdownMenu characters;

    public BattleAiModOptionsPanel() {
        ModLabel controllerModeLabel = new ModLabel(
                "", LABEL_X_POS / Settings.scale, LABEL_Y_POS / Settings.scale, Settings.CREAM_COLOR, FontHelper.charDescFont,
                this, modLabel -> {
            modLabel.text = "Client Controller Mode";
        });
        this.addUIElement(controllerModeLabel);

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

        characters.render(sb, LABEL_X_POS + 350 * Settings.scale, LABEL_Y_POS + 22 * Settings.scale);
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