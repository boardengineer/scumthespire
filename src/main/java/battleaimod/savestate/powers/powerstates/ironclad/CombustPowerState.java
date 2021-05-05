package battleaimod.savestate.powers.powerstates.ironclad;

import basemod.ReflectionHacks;
import battleaimod.savestate.powers.PowerState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.CombustPower;

public class CombustPowerState extends PowerState
{
    private final int hpLoss;

    public CombustPowerState(AbstractPower power) {
        super(power);
        this.hpLoss = ReflectionHacks
                .getPrivate(power, CombustPower.class, "hpLoss");
    }

    public CombustPowerState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.hpLoss = parsed.get("hp_loss").getAsInt();
    }

    @Override
    public String encode() {
        JsonObject result = new JsonParser().parse(super.encode()).getAsJsonObject();

        result.addProperty("hp_loss", hpLoss);

        return result.toString();
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        return new CombustPower(targetAndSource, hpLoss, amount);
    }
}
