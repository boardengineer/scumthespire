package autoplay.battleaimod.server.patches;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.desktop.DesktopLauncher;

@SpirePatch(
        clz = DesktopLauncher.class,
        paramtypez = {String[].class},
        method = "main"
)
public class DisableSound {

    public static void Prefix(String[] args) {
        LwjglApplicationConfiguration.disableAudio = true;
    }
}
