package battleaimod.savestate.monsters.city;

import basemod.ReflectionHacks;
import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.GremlinLeader;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class GremlinLeaderState extends MonsterState {
    public GremlinLeaderState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.GREMLIN_LEADER.ordinal();
    }

    public GremlinLeaderState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.GREMLIN_LEADER.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinLeader result = new GremlinLeader();
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = GremlinLeader.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 62)
        public static SpireReturn GremlinLeader(GremlinLeader _instance) {
            if (shouldGoFast()) {
                if (AbstractDungeon.ascensionLevel >= 8) {
                    MonsterState.setHp(_instance, 145, 155);
                } else {
                    MonsterState.setHp(_instance, 140, 148);
                }

                int strAmt;
                int blockAmt;

                if (AbstractDungeon.ascensionLevel >= 18) {
                    strAmt = 5;
                    blockAmt = 10;
                } else if (AbstractDungeon.ascensionLevel >= 3) {
                    strAmt = 4;
                    blockAmt = 6;
                } else {
                    strAmt = 3;
                    blockAmt = 6;
                }

                ReflectionHacks.setPrivate(_instance, GremlinLeader.class, "strAmt", strAmt);
                ReflectionHacks.setPrivate(_instance, GremlinLeader.class, "blockAmt", blockAmt);


                _instance.damage.add(new DamageInfo(_instance, 6));

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
