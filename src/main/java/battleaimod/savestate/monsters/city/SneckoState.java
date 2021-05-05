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
import com.megacrit.cardcrawl.monsters.city.Snecko;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class SneckoState extends MonsterState {
    private final boolean firstTurn;

    public SneckoState(AbstractMonster monster) {
        super(monster);

        firstTurn = ReflectionHacks
                .getPrivate(monster, Snecko.class, "firstTurn");

        monsterTypeNumber = Monster.SNECKO.ordinal();
    }

    public SneckoState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.firstTurn = parsed.get("first_turn").getAsBoolean();

        monsterTypeNumber = Monster.SNECKO.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Snecko monster = new Snecko(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, Snecko.class, "firstTurn", firstTurn);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("first_turn", firstTurn);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Snecko.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 53)
        public static SpireReturn Snecko(Snecko _instance) {
            if (shouldGoFast()) {
                int biteDmg;
                int tailDmg;

                if (AbstractDungeon.ascensionLevel >= 7) {
                    MonsterState.setHp(_instance, 120, 125);
                } else {
                    MonsterState.setHp(_instance, 114, 120);
                }

                if (AbstractDungeon.ascensionLevel >= 2) {
                    biteDmg = 18;
                    tailDmg = 10;
                } else {
                    biteDmg = 15;
                    tailDmg = 8;
                }

                _instance.damage.add(new DamageInfo(_instance, biteDmg));
                _instance.damage.add(new DamageInfo(_instance, tailDmg));

                ReflectionHacks
                        .setPrivate(_instance, Snecko.class, "biteDmg", biteDmg);
                ReflectionHacks
                        .setPrivate(_instance, Snecko.class, "tailDmg", tailDmg);

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
