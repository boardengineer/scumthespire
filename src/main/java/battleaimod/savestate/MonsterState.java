package battleaimod.savestate;

import basemod.ReflectionHacks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.monsters.beyond.Darkling;
import com.megacrit.cardcrawl.monsters.beyond.OrbWalker;
import com.megacrit.cardcrawl.monsters.city.*;
import com.megacrit.cardcrawl.monsters.exordium.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class MonsterState extends CreatureState {
    private static final String DAMAGE_DELIMETER = ";";
    private static final String MOVE_HISTORY_DELIMETER = "&";

    private final float deathTimer;
    private final boolean tintFadeOutCalled;
    private final boolean escaped;
    private final boolean escapeNext;
    private final boolean cannotEscape;
    private final byte nextMove;
    private final HitboxState intentHb;
    private final float intentAlpha;
    private final float intentAlphaTarget;
    private final float intentOffsetX;
    private final String moveName;
    private final EnemyMoveInfoState moveInfo;

    private final AbstractMonster.EnemyType type;

    private final AbstractMonster.Intent intent;
    private final AbstractMonster.Intent tipIntent;

    private final ArrayList<Byte> moveHistory;
    private final ArrayList<DamageInfoState> damage;

    private final int gremlinWizardCurrentCharge;

    private final int guardianDmgThreshold;
    private final int guardianDmgThesholdIncrease;
    private final int guardianDmgTaken;
    private final boolean guardianIsOpen;
    private final boolean guardianCloseUpTriggered;

    private final int lagavulinDebuffTurnCount;
    private final int lagavulinIdleCount;
    protected final boolean lagavulinIsAsleep;
    private final boolean lagavulinIsOut;
    private final boolean lagavulinIsOutTriggered;

    private final boolean louseIsOpen;
    private final int louseBiteDamage;

    private final boolean hexaghostActivated;
    private final boolean hexaghostBurnUpgraded;
    private final int hexaghostOrbActiveCount;
    private final List<Boolean> hexaghostActiveOrbs;

    private final boolean shelledParasiteFirstMove;

    private final boolean cultistFirstMove;

    private final boolean sphericGuardianFirstMove;
    private final boolean sphericGuardianSecondMove;

    private final boolean sneckoFirstTurn;

    private final int champNumTurns;
    private final int champForgeTimes;
    private final int champForgeThreshold;
    private final boolean champThresholdReached;
    private final boolean champFirstTurn;

    private final int automatonNumTurns;
    private final boolean automatonFirstTurn;

    protected final int bronzeOrbCount;
    private final boolean bronzeOrbUsedStasis;

    private final boolean chosenFirstTurn;
    private final boolean chosenUsedHex;

    private final int bookOfStabbingStabCount;

    private final int collectorTurnsTaken;
    private final boolean collectorUltUsed;
    private final boolean collectorInitialSpawn;

    private final int muggerSlashCount;
    private final int muggerStolenGold;

    protected final float offsetX;
    protected final float offsetY;

    public int monsterTypeNumber;

    public MonsterState(AbstractMonster monster) {
        super(monster);

        this.deathTimer = monster.deathTimer;
        this.tintFadeOutCalled = monster.tintFadeOutCalled;
        this.escaped = monster.escaped;
        this.escapeNext = monster.escapeNext;
        this.type = monster.type;
        this.cannotEscape = monster.cannotEscape;

        this.nextMove = monster.nextMove;
        this.intentHb = new HitboxState(monster.intentHb);
        this.intent = monster.intent;
        this.tipIntent = monster.tipIntent;
        this.intentAlpha = monster.intentAlpha;
        this.intentAlphaTarget = monster.intentAlphaTarget;
        this.intentOffsetX = monster.intentOffsetX;
        this.moveName = monster.moveName;

        this.moveInfo = new EnemyMoveInfoState((EnemyMoveInfo) ReflectionHacks
                .getPrivate(monster, AbstractMonster.class, "move"));

        this.damage = monster.damage.stream().map(DamageInfoState::new).map(damageInfoState -> {
            damageInfoState.owner = monster;
            return damageInfoState;
        }).collect(Collectors.toCollection(ArrayList::new));

        this.moveHistory = monster.moveHistory.stream().map(Byte::byteValue)
                                              .collect(Collectors.toCollection(ArrayList::new));

        if (monster instanceof GremlinWizard) {
            gremlinWizardCurrentCharge = ReflectionHacks
                    .getPrivate(monster, GremlinWizard.class, "currentCharge");
        } else {
            gremlinWizardCurrentCharge = 0;
        }

        if (monster instanceof TheGuardian) {
            guardianDmgThreshold = ReflectionHacks
                    .getPrivate(monster, TheGuardian.class, "dmgThreshold");
            guardianDmgThesholdIncrease = ReflectionHacks
                    .getPrivate(monster, TheGuardian.class, "dmgThresholdIncrease");
            guardianDmgTaken = ReflectionHacks
                    .getPrivate(monster, TheGuardian.class, "dmgTaken");
            guardianIsOpen = ReflectionHacks
                    .getPrivate(monster, TheGuardian.class, "isOpen");
            guardianCloseUpTriggered = ReflectionHacks
                    .getPrivate(monster, TheGuardian.class, "closeUpTriggered");
        } else {
            guardianDmgThreshold = 0;
            guardianDmgThesholdIncrease = 0;
            guardianDmgTaken = 0;
            guardianIsOpen = false;
            guardianCloseUpTriggered = false;
        }

        if (monster instanceof Lagavulin) {
            lagavulinIsAsleep = ReflectionHacks
                    .getPrivate(monster, Lagavulin.class, "asleep");
            lagavulinDebuffTurnCount = ReflectionHacks
                    .getPrivate(monster, Lagavulin.class, "debuffTurnCount");
            lagavulinIdleCount = ReflectionHacks
                    .getPrivate(monster, Lagavulin.class, "idleCount");
            lagavulinIsOut = ReflectionHacks
                    .getPrivate(monster, Lagavulin.class, "isOut");
            lagavulinIsOutTriggered = ReflectionHacks
                    .getPrivate(monster, Lagavulin.class, "isOutTriggered");
        } else {
            lagavulinDebuffTurnCount = 0;
            lagavulinIdleCount = 0;
            lagavulinIsAsleep = false;
            lagavulinIsOut = false;
            lagavulinIsOutTriggered = false;
        }

        if (monster instanceof LouseDefensive) {
            louseIsOpen = ReflectionHacks
                    .getPrivate(monster, LouseDefensive.class, "isOpen");
            louseBiteDamage = ReflectionHacks
                    .getPrivate(monster, LouseDefensive.class, "biteDamage");
        } else if (monster instanceof LouseNormal) {
            louseIsOpen = ReflectionHacks
                    .getPrivate(monster, LouseNormal.class, "isOpen");
            louseBiteDamage = ReflectionHacks
                    .getPrivate(monster, LouseNormal.class, "biteDamage");
        } else {
            louseIsOpen = false;
            louseBiteDamage = 0;
        }

        if (monster instanceof Hexaghost) {
            hexaghostActivated = ReflectionHacks
                    .getPrivate(monster, Hexaghost.class, "activated");
            hexaghostBurnUpgraded = ReflectionHacks
                    .getPrivate(monster, Hexaghost.class, "burnUpgraded");
            hexaghostOrbActiveCount = ReflectionHacks
                    .getPrivate(monster, Hexaghost.class, "orbActiveCount");
            ArrayList<HexaghostOrb> orbs = ReflectionHacks
                    .getPrivate(monster, Hexaghost.class, "orbs");
            hexaghostActiveOrbs = orbs.stream().map(orb -> orb.activated)
                                      .collect(Collectors.toList());
        } else {
            hexaghostActivated = false;
            hexaghostBurnUpgraded = false;
            hexaghostOrbActiveCount = 0;
            hexaghostActiveOrbs = new ArrayList<>();
        }

        if (monster instanceof ShelledParasite) {
            shelledParasiteFirstMove = ReflectionHacks
                    .getPrivate(monster, ShelledParasite.class, "firstMove");
        } else {
            shelledParasiteFirstMove = false;
        }

        if (monster instanceof Cultist) {
            cultistFirstMove = ReflectionHacks
                    .getPrivate(monster, Cultist.class, "firstMove");
        } else {
            cultistFirstMove = false;
        }

        if (monster instanceof SphericGuardian) {
            sphericGuardianFirstMove = ReflectionHacks
                    .getPrivate(monster, SphericGuardian.class, "firstMove");
            sphericGuardianSecondMove = ReflectionHacks
                    .getPrivate(monster, SphericGuardian.class, "secondMove");
        } else {
            sphericGuardianFirstMove = false;
            sphericGuardianSecondMove = false;
        }

        if (monster instanceof Snecko) {
            sneckoFirstTurn = ReflectionHacks
                    .getPrivate(monster, Snecko.class, "firstTurn");
        } else {
            sneckoFirstTurn = true;
        }

        if (monster instanceof Champ) {
            champNumTurns = ReflectionHacks
                    .getPrivate(monster, Champ.class, "numTurns");
            champForgeTimes = ReflectionHacks
                    .getPrivate(monster, Champ.class, "forgeTimes");
            champForgeThreshold = ReflectionHacks
                    .getPrivate(monster, Champ.class, "forgeThreshold");
            champThresholdReached = ReflectionHacks
                    .getPrivate(monster, Champ.class, "thresholdReached");
            champFirstTurn = ReflectionHacks
                    .getPrivate(monster, Champ.class, "firstTurn");
        } else {
            champNumTurns = 0;
            champForgeTimes = 0;
            champForgeThreshold = 0;
            champThresholdReached = false;
            champFirstTurn = false;
        }

        if (monster instanceof BronzeAutomaton) {
            automatonNumTurns = ReflectionHacks
                    .getPrivate(monster, BronzeAutomaton.class, "numTurns");
            automatonFirstTurn = ReflectionHacks
                    .getPrivate(monster, BronzeAutomaton.class, "firstTurn");
        } else {
            automatonNumTurns = 0;
            automatonFirstTurn = false;
        }

        if (monster instanceof BronzeOrb) {
            bronzeOrbCount = ReflectionHacks
                    .getPrivate(monster, BronzeOrb.class, "count");
            bronzeOrbUsedStasis = ReflectionHacks
                    .getPrivate(monster, BronzeOrb.class, "usedStasis");
        } else {
            bronzeOrbCount = 0;
            bronzeOrbUsedStasis = false;
        }

        if (monster instanceof Chosen) {
            chosenFirstTurn = ReflectionHacks
                    .getPrivate(monster, Chosen.class, "firstTurn");
            chosenUsedHex = ReflectionHacks
                    .getPrivate(monster, Chosen.class, "usedHex");
        } else {
            chosenFirstTurn = false;
            chosenUsedHex = false;
        }

        if (monster instanceof BookOfStabbing) {
            bookOfStabbingStabCount = ReflectionHacks
                    .getPrivate(monster, BookOfStabbing.class, "stabCount");
        } else {
            bookOfStabbingStabCount = 0;
        }

        if (monster instanceof TheCollector) {
            collectorInitialSpawn = ReflectionHacks
                    .getPrivate(monster, TheCollector.class, "initialSpawn");
            collectorTurnsTaken = ReflectionHacks
                    .getPrivate(monster, TheCollector.class, "turnsTaken");
            collectorUltUsed = ReflectionHacks
                    .getPrivate(monster, TheCollector.class, "ultUsed");
        } else {
            collectorInitialSpawn = false;
            collectorTurnsTaken = 0;
            collectorUltUsed = false;
        }

        if (monster instanceof Mugger) {
            muggerSlashCount = ReflectionHacks
                    .getPrivate(monster, Mugger.class, "slashCount");
            muggerStolenGold = ReflectionHacks
                    .getPrivate(monster, Mugger.class, "stolenGold");
        } else {
            muggerSlashCount = 0;
            muggerStolenGold = 0;
        }

        offsetX = (drawX - (float) Settings.WIDTH * 0.75F) / Settings.xScale;
        offsetY = (drawY - AbstractDungeon.floorY) / Settings.yScale;
    }

    public MonsterState(String jsonString) {
        super(new JsonParser().parse(jsonString).getAsJsonObject().get("creature").getAsString());

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.deathTimer = parsed.get("death_timer").getAsFloat();
        this.tintFadeOutCalled = parsed.get("tint_fade_out_called").getAsBoolean();
        this.escaped = parsed.get("escaped").getAsBoolean();
        this.escapeNext = parsed.get("escape_next").getAsBoolean();
        this.type = AbstractMonster.EnemyType.valueOf(parsed.get("type_name").getAsString());
        this.cannotEscape = parsed.get("cannot_escape").getAsBoolean();
        this.nextMove = parsed.get("next_move").getAsByte();
        this.intentHb = new HitboxState(parsed.get("intent_hb").getAsString());
        this.intent = AbstractMonster.Intent.valueOf(parsed.get("intent_name").getAsString());
        this.tipIntent = AbstractMonster.Intent
                .valueOf(parsed.get("tip_intent_name").getAsString());
        this.intentAlpha = parsed.get("intent_alpha").getAsFloat();
        this.intentAlphaTarget = parsed.get("intent_alpha_target").getAsFloat();
        this.intentOffsetX = parsed.get("intent_offset_x").getAsFloat();
        this.moveName = parsed.get("move_name").isJsonNull() ? null : parsed.get("move_name")
                                                                            .getAsString();

        this.moveInfo = new EnemyMoveInfoState(parsed.get("move_info").getAsString());

        this.damage = Stream.of(parsed.get("damage").getAsString().split(DAMAGE_DELIMETER))
                            .filter(s -> !s.isEmpty()).map(s -> new DamageInfoState(s))
                            .collect(Collectors.toCollection(ArrayList::new));

        this.moveHistory = Stream
                .of(parsed.get("move_history").getAsString().split(MOVE_HISTORY_DELIMETER))
                .filter(s -> !s.isEmpty()).map(Byte::parseByte)
                .collect(Collectors.toCollection(ArrayList::new));

        this.louseIsOpen = parsed.get("louse_is_open").getAsBoolean();
        this.louseBiteDamage = parsed.get("louse_bite_damage").getAsInt();

        this.gremlinWizardCurrentCharge = parsed.get("gremlin_wizard_current_charge").getAsInt();

        this.lagavulinDebuffTurnCount = parsed.get("lagavulin_debuff_turn_count").getAsInt();
        this.lagavulinIdleCount = parsed.get("lagavulin_idle_count").getAsInt();
        this.lagavulinIsAsleep = parsed.get("lagavulin_is_asleep").getAsBoolean();
        this.lagavulinIsOut = parsed.get("lagavulin_is_out").getAsBoolean();
        this.lagavulinIsOutTriggered = parsed.get("lagavulin_is_out_triggered").getAsBoolean();

        this.hexaghostActivated = parsed.get("hexaghost_activated").getAsBoolean();
        this.hexaghostBurnUpgraded = parsed.get("hexaghost_burn_upgraded").getAsBoolean();
        this.hexaghostOrbActiveCount = parsed.get("hexaghost_orb_active_count").getAsInt();
        ArrayList<Boolean> orbs = new ArrayList<>();
        parsed.get("hexaghost_active_orbs").getAsJsonArray()
              .forEach(active -> orbs.add(active.getAsBoolean()));
        this.hexaghostActiveOrbs = orbs;

        this.guardianDmgThreshold = parsed.get("guardian_dmg_threshold").getAsInt();
        this.guardianDmgThesholdIncrease = parsed.get("guardian_dmg_threshold_increase").getAsInt();
        this.guardianDmgTaken = parsed.get("guardian_dmg_taken").getAsInt();
        this.guardianIsOpen = parsed.get("guardian_is_open").getAsBoolean();
        this.guardianCloseUpTriggered = parsed.get("guardian_close_up_triggered").getAsBoolean();

        this.shelledParasiteFirstMove = parsed.get("shelled_parasite_first_move").getAsBoolean();

        this.cultistFirstMove = parsed.get("cultist_first_move").getAsBoolean();

        this.sphericGuardianFirstMove = parsed.get("spheric_guardian_first_move").getAsBoolean();
        this.sphericGuardianSecondMove = parsed.get("spheric_guardian_second_move").getAsBoolean();

        this.sneckoFirstTurn = parsed.get("snecko_first_turn").getAsBoolean();

        this.champNumTurns = parsed.get("champ_num_turns").getAsInt();
        this.champForgeTimes = parsed.get("champ_forge_times").getAsInt();
        this.champForgeThreshold = parsed.get("champ_forge_threshold").getAsInt();
        this.champThresholdReached = parsed.get("champ_threshold_reached").getAsBoolean();
        this.champFirstTurn = parsed.get("champ_first_turn").getAsBoolean();

        this.automatonNumTurns = parsed.get("automaton_num_turns").getAsInt();
        this.automatonFirstTurn = parsed.get("automaton_first_turn").getAsBoolean();

        this.bronzeOrbCount = parsed.get("bronze_orb_count").getAsInt();
        this.bronzeOrbUsedStasis = parsed.get("bronze_orb_used_stasis").getAsBoolean();

        this.chosenUsedHex = parsed.get("chosen_used_hex").getAsBoolean();
        this.chosenFirstTurn = parsed.get("chosen_first_turn").getAsBoolean();

        this.bookOfStabbingStabCount = parsed.get("book_of_stabbing_stab_count").getAsInt();

        this.collectorUltUsed = parsed.get("collector_ult_used").getAsBoolean();
        this.collectorTurnsTaken = parsed.get("collector_turns_taken").getAsInt();
        this.collectorInitialSpawn = parsed.get("collector_initial_spawn").getAsBoolean();

        this.muggerSlashCount = parsed.get("mugger_slash_count").getAsInt();
        this.muggerStolenGold = parsed.get("mugger_stolen_gold").getAsInt();

        offsetX = (drawX - (float) Settings.WIDTH * 0.75F) / Settings.xScale;
        offsetY = (drawY - AbstractDungeon.floorY) / Settings.yScale;
    }

    public AbstractMonster loadMonster() {
        AbstractMonster monster = getMonsterFromId();

        populateSharedFields(monster);

        return monster;
    }

    public void populateSharedFields(AbstractMonster monster) {
        super.loadCreature(monster);
        monster.init();

        monster.deathTimer = this.deathTimer;
        monster.tintFadeOutCalled = this.tintFadeOutCalled;
        monster.escaped = this.escaped;
        monster.escapeNext = this.escapeNext;
        monster.type = this.type;
        monster.cannotEscape = this.cannotEscape;

        monster.damage = this.damage.stream().map(DamageInfoState::loadDamageInfo)
                                    .peek(state -> state.owner = monster)
                                    .collect(Collectors.toCollection(ArrayList::new));

        monster.setMove(moveName, moveInfo.nextMove, moveInfo.intent, moveInfo.baseDamage, moveInfo.multiplier, moveInfo.isMultiDamage);

        monster.moveHistory = this.moveHistory.stream()
                                              .collect(Collectors.toCollection(ArrayList::new));

        monster.nextMove = this.nextMove;
        monster.intentHb = this.intentHb.loadHitbox();
        monster.intent = this.intent;
        monster.tipIntent = this.tipIntent;
        monster.intentAlpha = this.intentAlpha;
        monster.intentAlphaTarget = this.intentAlphaTarget;
        monster.intentOffsetX = this.intentOffsetX;
        monster.moveName = this.moveName;


//        monster.tint = new TintEffect();
//        monster.healthBarUpdatedEvent();
        monster.showHealthBar();
        monster.createIntent();

        if (!shouldGoFast() && monster.currentBlock > 0) {
            ReflectionHacks
                    .setPrivate(monster, AbstractCreature.class, "blockAnimTimer", 0.7F);
            ReflectionHacks
                    .setPrivate(monster, AbstractCreature.class, "blockTextColor", 0.0F);
        }

//        monster.updatePowers();

        if (monster instanceof GremlinWizard) {
            ReflectionHacks
                    .setPrivate(monster, GremlinWizard.class, "currentCharge", gremlinWizardCurrentCharge);
        }

        if (monster instanceof TheGuardian) {
            ReflectionHacks
                    .setPrivate(monster, TheGuardian.class, "dmgThreshold", guardianDmgThreshold);
            ReflectionHacks
                    .setPrivate(monster, TheGuardian.class, "dmgTaken", guardianDmgTaken);
            ReflectionHacks
                    .setPrivate(monster, TheGuardian.class, "dmgThresholdIncrease", guardianDmgThesholdIncrease);
            ReflectionHacks
                    .setPrivate(monster, TheGuardian.class, "isOpen", guardianIsOpen);
            ReflectionHacks
                    .setPrivate(monster, TheGuardian.class, "closeUpTriggered", guardianCloseUpTriggered);
        }

        if (monster instanceof Lagavulin) {
            ReflectionHacks
                    .setPrivate(monster, Lagavulin.class, "debuffTurnCount", lagavulinDebuffTurnCount);
            ReflectionHacks
                    .setPrivate(monster, Lagavulin.class, "idleCount", lagavulinIdleCount);
            ReflectionHacks
                    .setPrivate(monster, Lagavulin.class, "asleep", lagavulinIsAsleep);
            ReflectionHacks
                    .setPrivate(monster, Lagavulin.class, "isOut", lagavulinIsOut);
            ReflectionHacks
                    .setPrivate(monster, Lagavulin.class, "isOutTriggered", lagavulinIsOutTriggered);
        }

        if (monster instanceof LouseDefensive) {
            ReflectionHacks
                    .setPrivate(monster, LouseDefensive.class, "isOpen", louseIsOpen);
            ReflectionHacks
                    .setPrivate(monster, LouseDefensive.class, "biteDamage", louseBiteDamage);
        } else if (monster instanceof LouseNormal) {
            ReflectionHacks
                    .setPrivate(monster, LouseNormal.class, "isOpen", louseIsOpen);
            ReflectionHacks
                    .setPrivate(monster, LouseNormal.class, "biteDamage", louseBiteDamage);
        }

        if (monster instanceof Hexaghost) {
            ReflectionHacks
                    .setPrivate(monster, Hexaghost.class, "activated", hexaghostActivated);
            ReflectionHacks
                    .setPrivate(monster, Hexaghost.class, "burnUpgraded", hexaghostBurnUpgraded);
            ReflectionHacks
                    .setPrivate(monster, Hexaghost.class, "orbActiveCount", hexaghostOrbActiveCount);
            ArrayList<HexaghostOrb> orbs = ReflectionHacks
                    .getPrivate(monster, Hexaghost.class, "orbs");
            for (int i = 0; i < hexaghostActiveOrbs.size(); i++) {
                if (hexaghostActiveOrbs.get(i)) {
                    orbs.get(i).activate(0, 0);
                }
            }
        }

        if (monster instanceof ShelledParasite) {
            ReflectionHacks
                    .setPrivate(monster, ShelledParasite.class, "firstMove", shelledParasiteFirstMove);
        }

        if (monster instanceof Cultist) {
            ReflectionHacks
                    .setPrivate(monster, Cultist.class, "firstMove", cultistFirstMove);
        }

        if (monster instanceof SphericGuardian) {
            ReflectionHacks
                    .setPrivate(monster, SphericGuardian.class, "firstMove", sphericGuardianFirstMove);
            ReflectionHacks
                    .setPrivate(monster, SphericGuardian.class, "secondMove", sphericGuardianSecondMove);
        }

        if (monster instanceof Snecko) {
            ReflectionHacks
                    .setPrivate(monster, Snecko.class, "firstTurn", sneckoFirstTurn);
        }

        if (monster instanceof Champ) {
            ReflectionHacks
                    .setPrivate(monster, Champ.class, "numTurns", champNumTurns);
            ReflectionHacks
                    .setPrivate(monster, Champ.class, "forgeTimes", champForgeTimes);
            ReflectionHacks
                    .setPrivate(monster, Champ.class, "forgeThreshold", champForgeThreshold);
            ReflectionHacks
                    .setPrivate(monster, Champ.class, "thresholdReached", champThresholdReached);
            ReflectionHacks
                    .setPrivate(monster, Champ.class, "firstTurn", champFirstTurn);
        }

        if (monster instanceof BronzeAutomaton) {
            ReflectionHacks
                    .setPrivate(monster, BronzeAutomaton.class, "numTurns", automatonNumTurns);
            ReflectionHacks
                    .setPrivate(monster, BronzeAutomaton.class, "firstTurn", automatonFirstTurn);
        }

        if (monster instanceof BronzeOrb) {
            ReflectionHacks.setPrivate(monster, BronzeOrb.class, "usedStasis", bronzeOrbUsedStasis);
        }

        if (monster instanceof Chosen) {
            ReflectionHacks.setPrivate(monster, Chosen.class, "usedHex", chosenUsedHex);
            ReflectionHacks.setPrivate(monster, Chosen.class, "firstTurn", chosenFirstTurn);
        }

        if (monster instanceof BookOfStabbing) {
            ReflectionHacks
                    .setPrivate(monster, BookOfStabbing.class, "stabCount", bookOfStabbingStabCount);
        }

        if (monster instanceof TheCollector) {
            ReflectionHacks
                    .setPrivate(monster, TheCollector.class, "initialSpawn", collectorInitialSpawn);
            ReflectionHacks
                    .setPrivate(monster, TheCollector.class, "turnsTaken", collectorTurnsTaken);
            ReflectionHacks
                    .setPrivate(monster, TheCollector.class, "ultUsed", collectorUltUsed);
        }

        if (monster instanceof Mugger) {
            ReflectionHacks
                    .setPrivate(monster, Mugger.class, "slashCount", muggerSlashCount);
            ReflectionHacks
                    .setPrivate(monster, Mugger.class, "stolenGold", muggerStolenGold);
        }
    }

    private AbstractMonster getMonsterFromId() {
        System.err.println("this is still happening, why?");
        float offsetX = (drawX - (float) Settings.WIDTH * 0.75F) / Settings.xScale;
        float offsetY = (drawY - AbstractDungeon.floorY) / Settings.yScale;

        AbstractMonster monster = null;
        if (id.equals("AcidSlime_L")) {
            monster = new AcidSlime_L(offsetX, offsetY);
        } else if (id.equals("AcidSlime_M")) {
            monster = new AcidSlime_M(offsetX, offsetY);
        } else if (id.equals("AcidSlime_S")) {
            monster = new AcidSlime_S(offsetX, offsetY, 0);
        } else if (id.equals("Apology Slime")) {
            monster = new ApologySlime();
        } else if (id.equals("Cultist")) {
            monster = new Cultist(offsetX, offsetY, false);
        } else if (id.equals("FungiBeast")) {
            monster = new FungiBeast(offsetX, offsetY);
        } else if (id.equals("GremlinFat")) {
            monster = new GremlinFat(offsetX, offsetY);
        } else if (id.equals("GremlinNob")) {
            monster = new GremlinNob(offsetX, offsetY);
        } else if (id.equals("GremlinThief")) {
            monster = new GremlinThief(offsetX, offsetY);
        } else if (id.equals("GremlinTsundere")) {
            monster = new GremlinTsundere(offsetX, offsetY);
        } else if (id.equals("GremlinWarrior")) {
            monster = new GremlinWarrior(offsetX, offsetY);
        } else if (id.equals("GremlinWizard")) {
            monster = new GremlinWizard(offsetX, offsetY);
        } else if (id.equals("Hexaghost")) {
            monster = new Hexaghost();
        } else if (id.equals("JawWorm")) {
            try {
                monster = new JawWorm(offsetX, offsetY);
            } catch (Exception e) {
                System.err.println(e);
            }
        } else if (id.equals("Lagavulin")) {
            monster = new Lagavulin(false);
        } else if (id.equals("Looter")) {
            monster = new Looter(offsetX, offsetY);
        } else if (id.equals("FuzzyLouseDefensive")) {
            monster = new LouseDefensive(offsetX, offsetY);
        } else if (id.equals("FuzzyLouseNormal")) {
            monster = new LouseNormal(offsetX, offsetY);
        } else if (id.equals("Sentry")) {
            monster = new Sentry(offsetX, offsetY);
        } else if (id.equals("SlaverBlue")) {
            monster = new SlaverBlue(offsetX, offsetY);
        } else if (id.equals("SlaverRed")) {
            monster = new SlaverRed(offsetX, offsetY);
        } else if (id.equals("SlimeBoss")) {
            monster = new SlimeBoss();
        } else if (id.equals("SpikeSlime_L")) {
            monster = new SpikeSlime_L(offsetX, offsetY);
        } else if (id.equals("SpikeSlime_M")) {
            monster = new SpikeSlime_M(offsetX, offsetY);
        } else if (id.equals("SpikeSlime_S")) {
            monster = new SpikeSlime_S(offsetX, offsetY, 0);
        } else if (id.equals("TheGuardian")) {
            monster = new TheGuardian();
        } else if (id.equals("Chosen")) {
            monster = new Chosen();
        } else if (id.equals("Mugger")) {
            monster = new Mugger(offsetX, offsetY);
        } else if (id.equals("Shelled Parasite")) {
            monster = new ShelledParasite();
        } else if (id.equals("SphericGuardian")) {
            monster = new SphericGuardian();
        } else if (id.equals("GremlinLeader")) {
            monster = new GremlinLeader();
        } else if (id.equals("Byrd")) {
            monster = new Byrd(offsetX, offsetY);
        } else if (id.equals("SnakePlant")) {
            monster = new SnakePlant(offsetX, offsetY);
        } else if (id.equals("BookOfStabbing")) {
            monster = new BookOfStabbing();
        } else if (id.equals("BanditChild")) {
            monster = new BanditPointy(offsetX, offsetY);
        } else if (id.equals("BanditLeader")) {
            monster = new BanditLeader(offsetX, offsetY);
        } else if (id.equals("BanditBear")) {
            monster = new BanditBear(offsetX, offsetY);
        } else if (id.equals("SlaverBoss")) {
            monster = new Taskmaster(offsetX, offsetY);
        } else if (id.equals("Centurion")) {
            monster = new Centurion(offsetX, offsetY);
        } else if (id.equals("Healer")) {
            monster = new Healer(offsetX, offsetY);
        } else if (id.equals("Snecko")) {
            monster = new Snecko();
        } else if (id.equals("Champ")) {
            monster = new Champ();
        } else if (id.equals("Orb Walker")) {
            monster = new OrbWalker(offsetX, offsetY);
        } else if (id.equals("Darkling")) {
            monster = new Darkling(offsetX, offsetY);
        } else if (id.equals("BronzeAutomaton")) {
            monster = new BronzeAutomaton();
        } else if (id.equals("BronzeOrb")) {
            monster = new BronzeOrb(offsetX, offsetY, bronzeOrbCount);
        } else if (id.equals("TheCollector")) {
            monster = new TheCollector();
        } else if (id.equals("TorchHead")) {
            monster = new TorchHead(offsetX, offsetY);
        } else {
            System.err.println("couldn't find monster with id " + id);
        }

        if (!shouldGoFast()) {
            monster.update();
        }

        return monster;
    }

    public String encode() {
        JsonObject monsterStateJson = new JsonObject();

        monsterStateJson.addProperty("creature", super.encode());
        monsterStateJson.addProperty("death_timer", deathTimer);
        monsterStateJson.addProperty("tint_fade_out_called", tintFadeOutCalled);
        monsterStateJson.addProperty("escaped", escaped);
        monsterStateJson.addProperty("escape_next", escapeNext);
        monsterStateJson.addProperty("cannot_escape", cannotEscape);
        monsterStateJson.addProperty("next_move", nextMove);
        monsterStateJson.addProperty("intent_hb", intentHb.encode());
        monsterStateJson.addProperty("intent_alpha", intentAlpha);
        monsterStateJson.addProperty("intent_alpha_target", intentAlphaTarget);
        monsterStateJson.addProperty("intent_offset_x", intentOffsetX);
        monsterStateJson.addProperty("move_name", moveName);
        monsterStateJson.addProperty("move_info", moveInfo.encode());
        monsterStateJson.addProperty("intent_name", intent.name());
        monsterStateJson.addProperty("type_name", type.name());
        monsterStateJson.addProperty("tip_intent_name", tipIntent.name());

        monsterStateJson
                .addProperty("move_history", moveHistory.stream().map(b -> String.valueOf(b))
                                                        .collect(Collectors
                                                                .joining(MOVE_HISTORY_DELIMETER)));

        monsterStateJson.addProperty("damage", damage.stream().map(DamageInfoState::encode)
                                                     .collect(Collectors
                                                             .joining(DAMAGE_DELIMETER)));

        monsterStateJson.addProperty("louse_is_open", louseIsOpen);
        monsterStateJson.addProperty("louse_bite_damage", louseBiteDamage);

        monsterStateJson.addProperty("gremlin_wizard_current_charge", gremlinWizardCurrentCharge);

        monsterStateJson.addProperty("lagavulin_is_asleep", lagavulinIsAsleep);
        monsterStateJson.addProperty("lagavulin_is_out", lagavulinIsOut);
        monsterStateJson.addProperty("lagavulin_idle_count", lagavulinIdleCount);
        monsterStateJson.addProperty("lagavulin_debuff_turn_count", lagavulinDebuffTurnCount);
        monsterStateJson.addProperty("lagavulin_is_out_triggered", lagavulinIsOutTriggered);

        monsterStateJson.addProperty("hexaghost_activated", hexaghostActivated);
        monsterStateJson.addProperty("hexaghost_burn_upgraded", hexaghostBurnUpgraded);
        monsterStateJson.addProperty("hexaghost_orb_active_count", hexaghostOrbActiveCount);
        JsonArray orbArray = new JsonArray();
        hexaghostActiveOrbs.forEach(isActive -> orbArray.add(isActive));
        monsterStateJson.add("hexaghost_active_orbs", orbArray);

        monsterStateJson.addProperty("guardian_dmg_threshold", guardianDmgThreshold);
        monsterStateJson
                .addProperty("guardian_dmg_threshold_increase", guardianDmgThesholdIncrease);
        monsterStateJson.addProperty("guardian_dmg_taken", guardianDmgTaken);
        monsterStateJson.addProperty("guardian_is_open", guardianIsOpen);
        monsterStateJson.addProperty("guardian_close_up_triggered", guardianCloseUpTriggered);

        monsterStateJson.addProperty("shelled_parasite_first_move", shelledParasiteFirstMove);

        monsterStateJson.addProperty("cultist_first_move", cultistFirstMove);

        monsterStateJson.addProperty("spheric_guardian_first_move", sphericGuardianFirstMove);
        monsterStateJson.addProperty("spheric_guardian_second_move", sphericGuardianSecondMove);

        monsterStateJson.addProperty("snecko_first_turn", sneckoFirstTurn);

        monsterStateJson.addProperty("champ_num_turns", champNumTurns);
        monsterStateJson.addProperty("champ_forge_times", champForgeTimes);
        monsterStateJson.addProperty("champ_forge_threshold", champForgeThreshold);
        monsterStateJson.addProperty("champ_threshold_reached", champThresholdReached);
        monsterStateJson.addProperty("champ_first_turn", champFirstTurn);

        monsterStateJson.addProperty("automaton_num_turns", automatonNumTurns);
        monsterStateJson.addProperty("automaton_first_turn", automatonFirstTurn);

        monsterStateJson.addProperty("bronze_orb_count", bronzeOrbCount);
        monsterStateJson.addProperty("bronze_orb_used_stasis", bronzeOrbUsedStasis);

        monsterStateJson.addProperty("chosen_used_hex", chosenUsedHex);
        monsterStateJson.addProperty("chosen_first_turn", chosenFirstTurn);

        monsterStateJson.addProperty("book_of_stabbing_stab_count", bookOfStabbingStabCount);

        monsterStateJson.addProperty("collector_initial_spawn", collectorInitialSpawn);
        monsterStateJson.addProperty("collector_turns_taken", collectorTurnsTaken);
        monsterStateJson.addProperty("collector_ult_used", collectorUltUsed);

        monsterStateJson.addProperty("mugger_slash_count", muggerSlashCount);
        monsterStateJson.addProperty("mugger_stolen_gold", muggerStolenGold);

        return monsterStateJson.toString();
    }
}
