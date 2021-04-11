package battleaimod.savestate;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.*;

public class PowerState {
    private final String powerId;
    private final int amount;

    private final int hpLoss;

    public PowerState(AbstractPower power) {
        this.powerId = power.ID;
        this.amount = power.amount;

        if (power instanceof CombustPower) {
            this.hpLoss = ReflectionHacks
                    .getPrivate(power, CombustPower.class, "hpLoss");
        } else {
            this.hpLoss = 0;
        }
    }

    public PowerState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.powerId = parsed.get("power_id").getAsString();
        this.amount = parsed.get("amount").getAsInt();

        // TODO
        this.hpLoss = 0;
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
        } else if (powerId.equals("Curl Up")) {
            result = new CurlUpPower(targetAndSource, amount);
        } else if (powerId.equals("Flex")) {
            result = new LoseStrengthPower(targetAndSource, amount);
        } else if (powerId.equals("Artifact")) {
            result = new ArtifactPower(targetAndSource, amount);
        } else if (powerId.equals("Double Tap")) {
            result = new DoubleTapPower(targetAndSource, amount);
        } else if (powerId.equals("Split")) {
            result = new SplitPower(targetAndSource);
        } else if (powerId.equals("Combust")) {
            result = new CombustPower(targetAndSource, hpLoss, amount);
        } else if (powerId.equals("Evolve")) {
            result = new EvolvePower(targetAndSource, amount);
        } else if (powerId.equals("Mode Shift")) {
            result = new ModeShiftPower(targetAndSource, amount);
        } else if (powerId.equals("Angry")) {
            result = new AngryPower(targetAndSource, amount);
        } else if (powerId.equals("Sharp Hide")) {
            result = new SharpHidePower(targetAndSource, amount);
        } else if (powerId.equals("Entangled")) {
            result = new EntanglePower(targetAndSource);
        } else if (powerId.equals("Pen Nib")) {
            result = new PenNibPower(targetAndSource, amount);
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
