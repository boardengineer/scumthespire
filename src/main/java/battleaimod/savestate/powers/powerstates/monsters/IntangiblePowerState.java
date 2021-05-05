package battleaimod.savestate.powers.powerstates.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.powers.PowerState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.IntangiblePower;

public class IntangiblePowerState extends PowerState
{
    private final boolean justApplied;

    public IntangiblePowerState(AbstractPower power) {
        super(power);

        this.justApplied = ReflectionHacks
                .getPrivate(power, IntangiblePower.class, "justApplied");
    }

    public IntangiblePowerState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.justApplied = parsed.get("just_applied").getAsBoolean();
    }

    @Override
    public String encode() {
        JsonObject parsed = new JsonParser().parse(super.encode()).getAsJsonObject();

        parsed.addProperty("just_applied", justApplied);

        return parsed.toString();
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        IntangiblePower result = new IntangiblePower(targetAndSource, amount);
        ReflectionHacks
                .setPrivate(result, IntangiblePower.class, "justApplied", justApplied);
        return result;
    }
}
