package battleaimod.savestate.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinWizard;

public class GremlinWizardState extends MonsterState {
    private final int currentCharge;

    public GremlinWizardState(AbstractMonster monster) {
        super(monster);

        currentCharge = ReflectionHacks
                .getPrivate(monster, GremlinWizard.class, "currentCharge");

        monsterTypeNumber = Monster.GREMLIN_WIZARD.ordinal();
    }

    public GremlinWizardState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.currentCharge = parsed.get("current_charge").getAsInt();

        monsterTypeNumber = Monster.GREMLIN_WIZARD.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinWizard monster = new GremlinWizard(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, GremlinWizard.class, "currentCharge", currentCharge);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("current_charge", currentCharge);

        return monsterStateJson.toString();
    }
}
