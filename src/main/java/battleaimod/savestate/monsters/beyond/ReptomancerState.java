package battleaimod.savestate.monsters.beyond;

import basemod.ReflectionHacks;
import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.Reptomancer;

import java.util.ArrayList;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class ReptomancerState extends MonsterState {
    private final boolean firstMove;
    public ArrayList<Integer> daggerIndeces;

    public ReptomancerState(AbstractMonster monster) {
        super(monster);

        this.firstMove = ReflectionHacks.getPrivate(monster, Reptomancer.class, "firstMove");

        AbstractCreature[] daggers = ReflectionHacks
                .getPrivate(monster, Reptomancer.class, "daggers");
        daggerIndeces = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            AbstractCreature dagger = daggers[i];

            ArrayList<AbstractMonster> allMonsters = AbstractDungeon.getMonsters().monsters;

            boolean found = false;
            if (dagger != null) {
                for (int j = 0; j < allMonsters.size(); j++) {
                    if (allMonsters.get(j) == dagger) {
                        daggerIndeces.add(j);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                daggerIndeces.add(null);
            }
        }

        monsterTypeNumber = Monster.REPTOMANCER.ordinal();
    }

    public ReptomancerState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.firstMove = parsed.get("fist_move").getAsBoolean();
        JsonArray dIndeces = parsed.get("dagger_indeces").getAsJsonArray();

        daggerIndeces = new ArrayList<>();
        for (JsonElement index : dIndeces) {
            if (index.isJsonNull()) {
                daggerIndeces.add(null);
            } else {
                daggerIndeces.add(index.getAsInt());
            }
        }

        monsterTypeNumber = Monster.REPTOMANCER.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Reptomancer monster = new Reptomancer();

        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, Reptomancer.class, "firstMove", firstMove);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("fist_move", firstMove);

        JsonArray dIndeces = new JsonArray();
        daggerIndeces.forEach((index) -> dIndeces.add(index));
        monsterStateJson.add("dagger_indeces", dIndeces);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Reptomancer.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {

        @SpireInsertPatch(loc = 48)
        public static SpireReturn Insert(Reptomancer _instance) {
            if (shouldGoFast()) {
                int daggersPerSpawn;

                if (AbstractDungeon.ascensionLevel >= 18) {
                    daggersPerSpawn = 2;
                } else {
                    daggersPerSpawn = 1;
                }
                if (AbstractDungeon.ascensionLevel >= 8) {
                    MonsterState.setHp(_instance, 190, 200);
                } else {
                    MonsterState.setHp(_instance, 180, 190);
                }
                if (AbstractDungeon.ascensionLevel >= 3) {
                    _instance.damage.add(new DamageInfo(_instance, 16));
                    _instance.damage.add(new DamageInfo(_instance, 34));
                } else {
                    _instance.damage.add(new DamageInfo(_instance, 13));
                    _instance.damage.add(new DamageInfo(_instance, 30));
                }

                ReflectionHacks
                        .setPrivate(_instance, Reptomancer.class, "daggersPerSpawn", daggersPerSpawn);

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    public void postMonstersLoad(Reptomancer reptomancer) {
        ArrayList<AbstractMonster> allMonsters = AbstractDungeon.getMonsters().monsters;

        AbstractCreature[] daggers = ReflectionHacks
                .getPrivate(reptomancer, Reptomancer.class, "daggers");

        for (int i = 0; i < daggerIndeces.size(); i++) {
            if (daggerIndeces.get(i) == null) {
                daggers[i] = null;
            } else {
                if (daggerIndeces.get(i) < allMonsters.size()) {
                    daggers[i] = allMonsters.get(daggerIndeces.get(i));
                } else {
                    daggers[i] = null;
                }
            }
        }
    }
}
