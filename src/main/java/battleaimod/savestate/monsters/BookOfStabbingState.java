package battleaimod.savestate.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BookOfStabbing;

public class BookOfStabbingState extends MonsterState {
    private final int stabCount;

    public BookOfStabbingState(AbstractMonster monster) {
        super(monster);

        stabCount = ReflectionHacks
                .getPrivate(monster, BookOfStabbing.class, "stabCount");

        monsterTypeNumber = Monster.BOOK_OF_STABBING.ordinal();
    }

    public BookOfStabbingState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();
        this.stabCount = parsed.get("stab_count").getAsInt();

        monsterTypeNumber = Monster.BOOK_OF_STABBING.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        BookOfStabbing monster = new BookOfStabbing();

        ReflectionHacks
                .setPrivate(monster, BookOfStabbing.class, "stabCount", stabCount);

        populateSharedFields(monster);
        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("stab_count", stabCount);

        return monsterStateJson.toString();
    }
}
