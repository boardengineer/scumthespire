package battleaimod.savestate.powers.powerstates.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.actions.ActionState;
import battleaimod.savestate.powers.PowerState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.ConstrictedPower;

public class ConstrictedPowerState extends PowerState {
//    private final int sourceIndex;

    public ConstrictedPowerState(AbstractPower power) {
        super(power);

        AbstractCreature source = ReflectionHacks
                .getPrivate(power, ConstrictedPower.class, "source");
//        this.sourceIndex = ActionState.indexForCreature(source);
    }

    public ConstrictedPowerState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

//        this.sourceIndex = parsed.get("source_index").getAsInt();
    }

    @Override
    public String encode() {
        JsonObject parsed = new JsonParser().parse(super.encode()).getAsJsonObject();

//        parsed.addProperty("source_index", sourceIndex);

        return parsed.toString();
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        // TODO only spire growth does this for now and the reference gets lost sometimes
        return new ConstrictedPower(targetAndSource, AbstractDungeon.getMonsters().monsters
                .get(0), amount);
    }
}
