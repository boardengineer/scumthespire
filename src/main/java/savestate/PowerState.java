package savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.*;

public class PowerState {
    private final String powerId;
    private final int amount;

    public PowerState(AbstractPower power) {
        this.powerId = power.ID;
        this.amount = power.amount;
    }

    public PowerState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.powerId = parsed.get("power_id").getAsString();
        this.amount = parsed.get("amount").getAsInt();
    }

    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        AbstractPower result = null;
        if (powerId.equals("Strength")) {
            result = new StrengthPower(targetAndSource, amount);
        } else if (powerId.equals("Vulnerable")) {
            result = new VulnerablePower(targetAndSource, amount, false);
        } else if (powerId.equals("Ritual")) {
            result = new RitualPower(targetAndSource, amount, false);
        } else if (powerId.equals("Weakened")) {
            result = new WeakPower(targetAndSource, amount, false);
        } else if (powerId.equals("Frail")) {
            result = new FrailPower(targetAndSource, amount, false);
        } else if (powerId.equals("Anger")) {
            result = new AngerPower(targetAndSource, amount);
        } else if (powerId.equals("Spore Cloud")) {
            result = new SporeCloudPower(targetAndSource, amount);
        } else if (powerId.equals("Thievery")) {
            result = new ThieveryPower(targetAndSource, amount);
        } else if (powerId.equals("Metallicize")) {
            result = new MetallicizePower(targetAndSource, amount);
        } else if (powerId.equals("Dexterity")) {
            result = new DexterityPower(targetAndSource, amount);
        } else {
            System.err.println("missing type for power id: " + powerId);
        }

        return result;
    }

    public String encode() {
        JsonObject powerStateJson = new JsonObject();

        powerStateJson.addProperty("power_id", powerId);
        powerStateJson.addProperty("amount", amount);

        return powerStateJson.toString();
    }

}
