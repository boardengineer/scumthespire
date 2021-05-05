package battleaimod.savestate.powers.powerstates.common;

import basemod.ReflectionHacks;
import battleaimod.savestate.powers.PowerState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.WeakPower;

public class WeakenedPowerState extends PowerState
{
    private final boolean justApplied;

    public WeakenedPowerState(AbstractPower power) {
        super(power);

        this.justApplied = ReflectionHacks
                .getPrivate(power, WeakPower.class, "justApplied");
    }

    public WeakenedPowerState(String jsonString) {
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
        return new WeakPower(targetAndSource, amount, justApplied);
    }
}
