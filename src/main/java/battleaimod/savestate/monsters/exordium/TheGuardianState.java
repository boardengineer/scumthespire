package battleaimod.savestate.monsters.exordium;

import basemod.ReflectionHacks;
import battleaimod.fastobjects.AnimationStateDataFast;
import battleaimod.fastobjects.AnimationStateFast;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.TheGuardian;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class TheGuardianState extends MonsterState {
    private final int dmgThreshold;
    private final int dmgThresholdIncrease;
    private final int dmgTaken;
    private final boolean isOpen;
    private final boolean closeUpTriggered;

    public TheGuardianState(AbstractMonster monster) {
        super(monster);

        dmgThreshold = ReflectionHacks
                .getPrivate(monster, TheGuardian.class, "dmgThreshold");
        dmgThresholdIncrease = ReflectionHacks
                .getPrivate(monster, TheGuardian.class, "dmgThresholdIncrease");
        dmgTaken = ReflectionHacks
                .getPrivate(monster, TheGuardian.class, "dmgTaken");
        isOpen = ReflectionHacks
                .getPrivate(monster, TheGuardian.class, "isOpen");
        closeUpTriggered = ReflectionHacks
                .getPrivate(monster, TheGuardian.class, "closeUpTriggered");

        monsterTypeNumber = Monster.THE_GUARDIAN.ordinal();
    }

    public TheGuardianState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.dmgThreshold = parsed.get("dmg_threshold").getAsInt();
        this.dmgThresholdIncrease = parsed.get("dmg_threshold_increase").getAsInt();
        this.dmgTaken = parsed.get("dmg_taken").getAsInt();
        this.isOpen = parsed.get("is_open").getAsBoolean();
        this.closeUpTriggered = parsed.get("close_up_triggered").getAsBoolean();

        monsterTypeNumber = Monster.THE_GUARDIAN.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        TheGuardian monster = new TheGuardian();
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, TheGuardian.class, "dmgThreshold", dmgThreshold);
        ReflectionHacks
                .setPrivate(monster, TheGuardian.class, "dmgTaken", dmgTaken);
        ReflectionHacks
                .setPrivate(monster, TheGuardian.class, "dmgThresholdIncrease", dmgThresholdIncrease);
        ReflectionHacks
                .setPrivate(monster, TheGuardian.class, "isOpen", isOpen);
        ReflectionHacks
                .setPrivate(monster, TheGuardian.class, "closeUpTriggered", closeUpTriggered);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("dmg_threshold", dmgThreshold);
        monsterStateJson
                .addProperty("dmg_threshold_increase", dmgThresholdIncrease);
        monsterStateJson.addProperty("dmg_taken", dmgTaken);
        monsterStateJson.addProperty("is_open", isOpen);
        monsterStateJson.addProperty("close_up_triggered", closeUpTriggered);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = TheGuardian.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 100)
        public static SpireReturn TheGuardian(TheGuardian _instance) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();

                ReflectionHacks
                        .setPrivate(_instance, AbstractCreature.class, "stateData", new AnimationStateDataFast());

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
