package battleaimod.savestate.powers.powerstates.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.powers.PowerState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.MalleablePower;

public class MalleablePowerState extends PowerState
{
    private final int basePower;

    public MalleablePowerState(AbstractPower power) {
        super(power);

        this.basePower = ReflectionHacks
                .getPrivate(power, MalleablePower.class, "basePower");

    }

    public MalleablePowerState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.basePower = parsed.get("base_power").getAsInt();
    }

    @Override
    public String encode() {
        JsonObject parsed = new JsonParser().parse(super.encode()).getAsJsonObject();

        parsed.addProperty("base_power", basePower);

        return parsed.toString();
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        MalleablePower result = new MalleablePower(targetAndSource, amount);

        ReflectionHacks
                .setPrivate(result, MalleablePower.class, "basePower", basePower);

        return result;
    }
}
