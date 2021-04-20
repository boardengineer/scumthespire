package battleaimod.savestate.monsters;

import battleaimod.savestate.Monster;
import battleaimod.savestate.MonsterState;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.TorchHead;

public class TorchHeadState extends MonsterState {
    public TorchHeadState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.TORCH_HEAD.ordinal();
    }

    public TorchHeadState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.TORCH_HEAD.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        TorchHead result = new TorchHead(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }
}
