package communicationmod.savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class DamageInfoState {
    public AbstractCreature owner;
    private final String name;
    private final DamageInfo.DamageType type;
    private final int base;
    private final int output;
    public boolean isModified;

    public DamageInfoState(DamageInfo damageInfo) {
        this.name = damageInfo.name;
        this.type = damageInfo.type;
        this.base = damageInfo.base;
        this.output = damageInfo.output;
        this.isModified = damageInfo.isModified;
    }

    public DamageInfoState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.name = parsed.get("name").isJsonNull() ? null : parsed.get("name").getAsString();
        this.type = parsed.get("type_name").isJsonArray() ? null : DamageInfo.DamageType
                .valueOf(parsed.get("type_name").getAsString());
        this.base = parsed.get("base").getAsInt();
        this.output = parsed.get("output").getAsInt();
        this.isModified = parsed.get("is_modified").getAsBoolean();
    }

    public DamageInfo loadDamageInfo() {
        DamageInfo damageInfo = new DamageInfo(owner, base, type);
        damageInfo.name = name;
        damageInfo.output = output;
        damageInfo.isModified = isModified;
        return damageInfo;
    }

    public String encode() {
        JsonObject damageInfoStateJson = new JsonObject();

        damageInfoStateJson.addProperty("base", base);
        damageInfoStateJson.addProperty("type_name", type.name());
        damageInfoStateJson.addProperty("name", name);
        damageInfoStateJson.addProperty("output", output);
        damageInfoStateJson.addProperty("is_modified", isModified);

        return damageInfoStateJson.toString();
    }
}
