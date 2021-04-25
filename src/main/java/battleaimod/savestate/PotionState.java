package battleaimod.savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.potions.AbstractPotion;

public class PotionState {
    private final String potionId;

    public PotionState(AbstractPotion potion) {
        this.potionId = potion.ID;
    }

    public PotionState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.potionId = parsed.get("id").getAsString();
    }

    public String encode() {
        JsonObject potionJson = new JsonObject();

        potionJson.addProperty("id", potionId);

        return potionJson.toString();
    }

    public AbstractPotion loadPotion() {
        return PotionHelper.getPotion(potionId).makeCopy();
    }
}
