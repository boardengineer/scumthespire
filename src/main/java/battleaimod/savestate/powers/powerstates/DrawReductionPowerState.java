package battleaimod.savestate.powers.powerstates;

import basemod.ReflectionHacks;
import battleaimod.savestate.powers.PowerState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.DrawReductionPower;

public class DrawReductionPowerState extends PowerState
{
    private final boolean justApplied;

    public DrawReductionPowerState(AbstractPower power) {
        super(power);

        this.justApplied = ReflectionHacks
                .getPrivate(power, DrawReductionPower.class, "justApplied");
    }

    public DrawReductionPowerState(String jsonString) {
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
        DrawReductionPower result = new DrawReductionPower(targetAndSource, amount);
        ReflectionHacks
                .setPrivate(result, DrawReductionPower.class, "justApplied", justApplied);
        return result;
    }
}
