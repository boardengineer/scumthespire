package battleaimod.savestate.monsters.city;

import basemod.ReflectionHacks;
import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BronzeAutomaton;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

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

    @SpirePatch(
            clz = BronzeAutomaton.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 56)
        public static SpireReturn BronzeAutomaton(BronzeAutomaton _instance) {
            if (shouldGoFast()) {
                _instance.type = AbstractMonster.EnemyType.BOSS;

                int blockAmt;
                int flailDmg;
                int beamDmg;
                int strAmt;

                if (AbstractDungeon.ascensionLevel >= 9) {
                    MonsterState.setHp(_instance, 320, 320);
                    blockAmt = 12;
                } else {
                    MonsterState.setHp(_instance, 300, 300);
                    blockAmt = 9;
                }

                if (AbstractDungeon.ascensionLevel >= 4) {
                    flailDmg = 8;
                    beamDmg = 50;
                    strAmt = 4;
                } else {
                    flailDmg = 7;
                    beamDmg = 45;
                    strAmt = 3;
                }

                _instance.damage.add(new DamageInfo(_instance, flailDmg));
                _instance.damage.add(new DamageInfo(_instance, beamDmg));

                ReflectionHacks
                        .setPrivate(_instance, BronzeAutomaton.class, "blockAmt", blockAmt);
                ReflectionHacks
                        .setPrivate(_instance, BronzeAutomaton.class, "flailDmg", flailDmg);
                ReflectionHacks
                        .setPrivate(_instance, BronzeAutomaton.class, "beamDmg", beamDmg);
                ReflectionHacks
                        .setPrivate(_instance, BronzeAutomaton.class, "strAmt", strAmt);

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
