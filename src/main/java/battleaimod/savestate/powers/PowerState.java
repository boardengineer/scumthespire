package battleaimod.savestate.powers;

import battleaimod.BattleAiMod;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;

public class PowerState {
    public final String powerId;
    public final int amount;
    protected JsonObject jObject = null;

    public PowerState(AbstractPower power) {
        this.powerId = power.ID;
        this.amount = power.amount;
    }

    public PowerState(String jsonString) {
        jObject = new JsonParser().parse(jsonString).getAsJsonObject();

        this.powerId = jObject.get("power_id").getAsString();
        this.amount = jObject.get("amount").getAsInt();
    }

    public static PowerState forJsonString(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        String id = parsed.get("power_id").getAsString();

        return BattleAiMod.powerByIdmap.get(id).jsonFactory.apply(jsonString);
    }

    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        if (BattleAiMod.powerByIdmap.containsKey(powerId)) {
            return BattleAiMod.powerByIdmap.get(powerId).factory
                    .apply(new DummyPower(powerId, amount)).loadPower(targetAndSource);
        }

        throw new IllegalStateException("no known state for " + powerId);
    }

    public String encode() {
        if(jObject == null)
            jObject = new JsonObject();
    
        jObject.addProperty("power_id", powerId);
        jObject.addProperty("amount", amount);

        return jObject.toString();
    }

    // A generic empty power so that power factories can be used for basic json powers
    private class DummyPower extends AbstractPower {
        DummyPower(String powerId, int amount) {
            this.ID = powerId;
            this.amount = amount;
        }
    }
}
