package battleaimod.savestate.monsters;

import basemod.ReflectionHacks;
import battleaimod.savestate.CreatureState;
import battleaimod.savestate.DamageInfoState;
import battleaimod.savestate.EnemyMoveInfoState;
import battleaimod.savestate.HitboxState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.monsters.beyond.Darkling;
import com.megacrit.cardcrawl.monsters.beyond.OrbWalker;
import com.megacrit.cardcrawl.monsters.city.*;
import com.megacrit.cardcrawl.monsters.exordium.*;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public abstract class MonsterState extends CreatureState {
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

        if(!shouldGoFast()) {
            monster.showHealthBar();
            monster.healthBarUpdatedEvent();
        }

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
        monster.setIntentBaseDmg(moveInfo.baseDamage);

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

        if (!shouldGoFast()) {
            monster.showHealthBar();
            monster.createIntent();
        }

        if (!shouldGoFast() && monster.currentBlock > 0) {
            ReflectionHacks
                    .setPrivate(monster, AbstractCreature.class, "blockAnimTimer", 0.7F);
            ReflectionHacks
                    .setPrivate(monster, AbstractCreature.class, "blockTextColor", 0.0F);
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
            monster = new Lagavulin(true);
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
            monster = new BronzeOrb(offsetX, offsetY, 0);
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

        return monsterStateJson.toString();
    }

    // static copy of AbstractMonster.setHp
    public static void setHp(AbstractMonster monster, int minHp, int maxHp) {
        monster.currentHealth = AbstractDungeon.monsterHpRng.random(minHp, maxHp);
        if (Settings.isEndless && AbstractDungeon.player.hasBlight("ToughEnemies")) {
            float mod = AbstractDungeon.player.getBlight("ToughEnemies").effectFloat();
            monster.currentHealth = (int) ((float) monster.currentHealth * mod);
        }

        if (ModHelper.isModEnabled("MonsterHunter")) {
            monster.currentHealth = (int) ((float) monster.currentHealth * 1.5F);
        }

        monster.maxHealth = monster.currentHealth;
    }
}
