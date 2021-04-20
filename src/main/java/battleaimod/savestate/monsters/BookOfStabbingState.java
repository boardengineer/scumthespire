package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BookOfStabbing;

public class BookOfStabbingState extends MonsterState {
    public BookOfStabbingState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.BOOK_OF_STABBING.ordinal();
    }

    public BookOfStabbingState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.BOOK_OF_STABBING.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        BookOfStabbing result = new BookOfStabbing();
        populateSharedFields(result);
        return result;
    }
}
