package battleaimod.savestate;

import battleaimod.savestate.monsters.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.function.Function;

public enum Monster {
    ACID_SLIME_L("AcidSlime_L", monster -> new AcidSlime_LState(monster), json -> new AcidSlime_LState(json)),
    ACID_SLIME_M("AcidSlime_M", monster -> new AcidSlime_MState(monster), json -> new AcidSlime_MState(json)),
    ACID_SLIME_S("AcidSlime_S", monster -> new AcidSlime_SState(monster), json -> new AcidSlime_SState(json)),
    APOLOGY_SLIME("Apology Slime", monster -> new ApologySlimeState(monster), json -> new ApologySlimeState(json)),
    CULTIST("Cultist", monster -> new CultistState(monster), json -> new CultistState(json)),
    FUNGI_BEST("FungiBeast", monster -> new FungiBeastState(monster), json -> new FungiBeastState(json)),
    GREMLIN_FAT("GremlinFat", monster -> new GremlinFatState(monster), json -> new GremlinFatState(json)),
    GREMLIN_NOB("GremlinNob", monster -> new GremlinNobState(monster), json -> new GremlinNobState(json)),
    GREMLIN_THIEF("GremlinThief", monster -> new GremlinThiefState(monster), json -> new GremlinThiefState(json)),
    GREMLIN_TSUNDERE("GremlinTsundere", monster -> new GremlinTsundereState(monster), json -> new GremlinTsundereState(json)),
    GREMLIN_WARRIOR("GremlinWarrior", monster -> new GremlinWarriorState(monster), json -> new GremlinWarriorState(json)),
    GREMLIN_WIZARD("GremlinWizard", monster -> new GremlinWizardState(monster), json -> new GremlinWizardState(json)),
    HEXAGHOST("Hexaghost", monster -> new HexaghostState(monster), json -> new HexaghostState(json)),
    JAWWORM("JawWorm", monster -> new JawWormState(monster), json -> new JawWormState(json)),
    LAGAVULIN("Lagavulin", monster -> new LagaVulinState(monster), json -> new LagaVulinState(json)),
    LOOTER("Looter", monster -> new LooterState(monster), json -> new LooterState(json)),
    FUZZY_LOUSE_DEFENSIVE("FuzzyLouseDefensive", monster -> new LouseDefensiveState(monster), json -> new LouseDefensiveState(json)),
    FUZZY_LOUSE_NORMAL("FuzzyLouseNormal", monster -> new LouseNormalState(monster), json -> new LouseNormalState(json)),
    SENTRY("Sentry", monster -> new SentryState(monster), json -> new SentryState(json)),
    SLAVER_BLUE("SlaverBlue", monster -> new SlaverBlueState(monster), json -> new SlaverBlueState(json)),
    SLAVER_RED("SlaverRed", monster -> new SlaverRedState(monster), json -> new SlaverRedState(json)),
    SLIME_BOSS("SlimeBoss", monster -> new SlimeBossState(monster), json -> new SlimeBossState(json)),
    SPIKE_SLIME_L("SpikeSlime_L", monster -> new SpikeSlime_LState(monster), json -> new SpikeSlime_LState(json)),
    SPIKE_SLIME_M("SpikeSlime_M", monster -> new SpikeSlime_MState(monster), json -> new SpikeSlime_MState(json)),
    SPIKE_SLIME_S("SpikeSlime_S", monster -> new SpikeSlime_SState(monster), json -> new SpikeSlime_SState(json)),
    THE_GUARDIAN("TheGuardian", monster -> new TheGuardianState(monster), json -> new TheGuardianState(json)),
    CHOSEN("Chosen", monster -> new ChosenState(monster), json -> new ChosenState(json)),
    MUGGER("Mugger", monster -> new MuggerState(monster), json -> new MuggerState(json)),
    SHELLED_PARASITE("Shelled Parasite", monster -> new ShelledParasiteState(monster), json -> new ShelledParasiteState(json)),
    SPHERIC_GUARDIAN("SphericGuardian", monster -> new SphericGuardianState(monster), json -> new SphericGuardianState(json)),
    GREMLIN_LEADER("GremlinLeader", monster -> new GremlinLeaderState(monster), json -> new GremlinLeaderState(json)),
    BYRD("Byrd", monster -> new ByrdState(monster), json -> new ByrdState(json)),
    SNAKE_PLANT("SnakePlant", monster -> new SnakePlantState(monster), json -> new SnakePlantState(json)),
    BOOK_OF_STABBING("BookOfStabbing", monster -> new BookOfStabbingState(monster), json -> new BookOfStabbingState(json)),
    BANDIT_CHILD("BanditChild", monster -> new BanditPointyState(monster), json -> new BanditPointyState(json)),
    BANDIT_LEADER("BanditLeader", monster -> new BanditLeaderState(monster), json -> new BanditLeaderState(json)),
    BANDIT_BEAR("BanditBear", monster -> new BanditBearState(monster), json -> new BanditBearState(json)),
    SLAVER_BOSS("SlaverBoss", monster -> new TaskmasterState(monster), json -> new TaskmasterState(json)),
    CENTURION("Centurion", monster -> new CenturionState(monster), json -> new CenturionState(json)),
    HEALER("Healer", monster -> new HealerState(monster), json -> new HealerState(json)),
    SNECKO("Snecko", monster -> new SneckoState(monster), json -> new SneckoState(json)),
    CHAMP("Champ", monster -> new ChampState(monster), json -> new ChampState(json)),
    ORG_WALKER("Orb Walker", monster -> new OrbWalkerState(monster), json -> new OrbWalkerState(json)),
    DARKLING("Darkling", monster -> new DarklinkState(monster), json -> new DarklinkState(json)),
    BRONZE_AUTOMATON("BronzeAutomaton", monster -> new BronzeAutomatonState(monster), json -> new BronzeAutomatonState(json)),
    BRONZE_ORB("BronzeOrb", monster -> new BronzeOrbState(monster), json -> new BronzeOrbState(json)),
    THE_COLLECTOR("TheCollector", monster -> new TheCollectorState(monster), json -> new TheCollectorState(json)),
    TORCH_HEAD("TorchHead", monster -> new TorchHeadState(monster), json -> new TorchHeadState(json));

    public final String monsterId;
    public final Function<AbstractMonster, MonsterState> factory;
    public final Function<String, MonsterState> jsonFactory;

    Monster(String monsterId) {
        this.monsterId = monsterId;
        this.factory = moot -> null;
        this.jsonFactory = moot -> null;
    }

    Monster(String monsterId, Function<AbstractMonster, MonsterState> factory) {
        this.monsterId = monsterId;
        this.factory = factory;
        this.jsonFactory = moot -> null;
    }

    Monster(String monsterId, Function<AbstractMonster, MonsterState> factory, Function<String, MonsterState> jsonFactory) {
        this.monsterId = monsterId;
        this.factory = factory;
        this.jsonFactory = jsonFactory;
    }
}
