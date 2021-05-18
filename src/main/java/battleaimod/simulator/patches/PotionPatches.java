package battleaimod.simulator.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.PotionSlot;

import java.util.Iterator;

import static battleaimod.simulator.patches.MonsterPatch.shouldGoFast;

public class PotionPatches {
    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {AbstractPotion.class},
            method = "obtainPotion"
    )
    public static class QuietPotionsPatch {
        public static SpireReturn Prefix(AbstractPlayer _instance, AbstractPotion potionToObtain) {
            if (shouldGoFast()) {
                int index = 0;

                for (Iterator var3 = _instance.potions.iterator(); var3.hasNext(); ++index) {
                    AbstractPotion p = (AbstractPotion) var3.next();
                    if (p instanceof PotionSlot) {
                        break;
                    }
                }

                if (index < _instance.potionSlots) {
                    _instance.potions.set(index, potionToObtain);
                    potionToObtain.setAsObtained(index);
                    return SpireReturn.Return(true);
                } else {
                    AbstractDungeon.topPanel.flashRed();
                    return SpireReturn.Return(false);
                }
            }
            return SpireReturn.Continue();
        }
    }
}
