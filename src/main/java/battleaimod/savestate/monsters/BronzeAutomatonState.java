package battleaimod.savestate.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BronzeAutomaton;

public class BronzeAutomatonState extends MonsterState {
    private final int numTurns;
    private final boolean firstTurn;

    public BronzeAutomatonState(AbstractMonster monster) {
        super(monster);

        numTurns = ReflectionHacks
                .getPrivate(monster, BronzeAutomaton.class, "numTurns");
        firstTurn = ReflectionHacks
                .getPrivate(monster, BronzeAutomaton.class, "firstTurn");

        monsterTypeNumber = Monster.BRONZE_AUTOMATON.ordinal();
    }

    public BronzeAutomatonState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.numTurns = parsed.get("num_turns").getAsInt();
        this.firstTurn = parsed.get("first_turn").getAsBoolean();

        monsterTypeNumber = Monster.BRONZE_AUTOMATON.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        BronzeAutomaton monster = new BronzeAutomaton();
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, BronzeAutomaton.class, "numTurns", numTurns);
        ReflectionHacks
                .setPrivate(monster, BronzeAutomaton.class, "firstTurn", firstTurn);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("num_turns", numTurns);
        monsterStateJson.addProperty("first_turn", firstTurn);

        return monsterStateJson.toString();
    }
}
