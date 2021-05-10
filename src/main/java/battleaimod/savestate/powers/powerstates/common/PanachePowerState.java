package battleaimod.savestate.powers.powerstates.common;

import basemod.ReflectionHacks;
import battleaimod.savestate.powers.PowerState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.PanachePower;

public class PanachePowerState extends PowerState {
    private final int damage;

    public PanachePowerState(AbstractPower power) {
        super(power);

        this.damage = ReflectionHacks.getPrivate(power, PanachePower.class, "damage");
    }

    public PanachePowerState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.damage = parsed.get("damage").getAsInt();
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        PanachePower result = new PanachePower(targetAndSource, damage);

        result.amount = this.amount;

        return result;
    }

    @Override
    public String encode() {
        JsonObject parsed = new JsonParser().parse(super.encode()).getAsJsonObject();

        parsed.addProperty("damage", damage);

        return parsed.toString();
    }
}
