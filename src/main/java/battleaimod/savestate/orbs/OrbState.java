package battleaimod.savestate.orbs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.orbs.AbstractOrb;

public abstract class OrbState {
    public int evokeAmount = 0;
    public int passiveAmount = 0;
    public int lookupIndex;

    public OrbState(AbstractOrb orb, int lookupIndex) {
        this.evokeAmount = orb.evokeAmount;
        this.passiveAmount = orb.passiveAmount;
        this.lookupIndex = lookupIndex;
    }

    public OrbState(String jsonString, int lookupIndex) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.evokeAmount = parsed.get("evoke_amount").getAsInt();
        this.passiveAmount = parsed.get("passive_amount").getAsInt();
        this.lookupIndex = lookupIndex;
    }

    public static OrbState forJsonString(String jsonString) {
        System.err.println("parsing orb...");

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        int lookupIndex = parsed.get("lookup_index").getAsInt();

        return Orb.values()[lookupIndex].jsonFactory.apply(jsonString);
    }

    public String encode() {
        JsonObject result = new JsonObject();

        result.addProperty("evoke_amount", evokeAmount);
        result.addProperty("passive_amount", passiveAmount);
        result.addProperty("lookup_index", lookupIndex);

        return result.toString();

    }

    public abstract AbstractOrb loadOrb();
}
