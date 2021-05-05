package battleaimod.savestate.powers.powerstates.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.powers.PowerState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.FlightPower;

public class FlightPowerState extends PowerState
{
    private final int storedAmount;

    public FlightPowerState(AbstractPower power) {
        super(power);

        this.storedAmount = ReflectionHacks
                .getPrivate(power, FlightPower.class, "storedAmount");
    }

    public FlightPowerState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.storedAmount = parsed.get("stored_amount").getAsInt();
    }

    @Override
    public String encode() {
        JsonObject parsed = new JsonParser().parse(super.encode()).getAsJsonObject();

        parsed.addProperty("stored_amount", storedAmount);
        return parsed.toString();
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        FlightPower result = new FlightPower(targetAndSource, amount);
        ReflectionHacks
                .setPrivate(result, FlightPower.class, "storedAmount", storedAmount);
        return result;
    }
}
