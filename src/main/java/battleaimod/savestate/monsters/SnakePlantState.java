package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.SnakePlant;

public class SnakePlantState extends MonsterState {
    public SnakePlantState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.SNAKE_PLANT.ordinal();
    }

    public SnakePlantState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.SNAKE_PLANT.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SnakePlant result = new SnakePlant(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
