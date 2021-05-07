package battleaimod.savestate;

import battleaimod.BattleAiMod;
import battleaimod.savestate.powers.PowerState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: make encoderzz11aaSSSAAA
public class CreatureState {
    private static final String POWER_DELIMETER = ";;;";

    private final String name;
    public final String id;

    private final boolean isPlayer;
    private final boolean isBloodied;
    protected final float drawX;
    protected final float drawY;
    private final float dialogX;
    private final float dialogY;

    private final int gold;
    private final int displayGold;
    private final boolean isDying;
    private final boolean isDead;
    private final boolean halfDead;
    private final boolean flipHorizontal;
    private final boolean flipVertical;
    private final float escapeTimer;
    private final boolean isEscaping;
    private final int lastDamageTaken;
    private final float hb_x;
    private final float hb_y;
    private final float hb_w;
    private final float hb_h;
    public final int currentHealth;
    public final int maxHealth;
    public final int currentBlock;
    private final float hbAlpha;

    private final float animX;
    private final float animY;
    private final float reticleAlpha;
    private final boolean reticleRendered;

    private final HitboxState hb;
    private final HitboxState healthHb;

    public final ArrayList<PowerState> powers;

    public CreatureState(AbstractCreature creature) {
        this.name = creature.name;
        this.id = creature.id;
        this.powers = creature.powers.stream()
                                     .map(power -> BattleAiMod.powerByIdmap.get(power.ID).factory
                                             .apply(power))
                                     .collect(Collectors.toCollection(ArrayList::new));
        this.isPlayer = creature.isPlayer;
        this.isBloodied = creature.isBloodied;
        this.drawX = creature.drawX;
        this.drawY = creature.drawY;
        this.dialogX = creature.dialogX;
        this.dialogY = creature.dialogY;

        this.hb = new HitboxState(creature.hb);
        this.healthHb = new HitboxState(creature.healthHb);

        this.gold = creature.gold;
        this.displayGold = creature.displayGold;
        this.isDying = creature.isDying;
        this.isDead = creature.isDead;
        this.halfDead = creature.halfDead;
        this.flipHorizontal = creature.flipHorizontal;
        this.flipVertical = creature.flipVertical;
        this.escapeTimer = creature.escapeTimer;
        this.isEscaping = creature.isEscaping;
        this.lastDamageTaken = creature.lastDamageTaken;
        this.hb_x = creature.hb_x;
        this.hb_y = creature.hb_y;
        this.hb_w = creature.hb_w;
        this.hb_h = creature.hb_h;
        this.currentHealth = creature.currentHealth;
        this.maxHealth = creature.maxHealth;
        this.currentBlock = creature.currentBlock;
        this.hbAlpha = creature.hbAlpha;
        this.animX = creature.animX;
        this.animY = creature.animY;
        this.reticleAlpha = creature.reticleAlpha;
        this.reticleRendered = creature.reticleRendered;
    }

    public CreatureState(String jsonString) {
        long loadStartTime = System.currentTimeMillis();

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.name = parsed.get("name").getAsString();
        this.id = parsed.get("id").isJsonNull() ? null : parsed.get("id").getAsString();
        this.isPlayer = parsed.get("is_player").getAsBoolean();
        this.isBloodied = parsed.get("is_bloodied").getAsBoolean();
        this.drawX = parsed.get("draw_x").getAsInt();
        this.drawY = parsed.get("draw_y").getAsInt();
        this.dialogX = parsed.get("dialog_x").getAsInt();
        this.dialogY = parsed.get("dialog_y").getAsInt();
        this.gold = parsed.get("gold").getAsInt();
        this.displayGold = parsed.get("display_gold").getAsInt();
        this.isDying = parsed.get("is_dying").getAsBoolean();
        this.isDead = parsed.get("is_dead").getAsBoolean();
        this.halfDead = parsed.get("half_dead").getAsBoolean();
        this.flipHorizontal = parsed.get("flip_horizontal").getAsBoolean();
        this.flipVertical = parsed.get("flip_vertical").getAsBoolean();
        this.escapeTimer = parsed.get("escape_timer").getAsInt();
        this.isEscaping = parsed.get("is_escaping").getAsBoolean();
        this.lastDamageTaken = parsed.get("last_damage_taken").getAsInt();
        this.hb_x = parsed.get("hb_x").getAsInt();
        this.hb_y = parsed.get("hb_y").getAsInt();
        this.hb_w = parsed.get("hb_w").getAsInt();
        this.hb_h = parsed.get("hb_h").getAsInt();
        this.currentHealth = parsed.get("current_health").getAsInt();
        this.maxHealth = parsed.get("max_health").getAsInt();
        this.currentBlock = parsed.get("current_block").getAsInt();
        this.hbAlpha = parsed.get("hb_alpha").getAsInt();
        this.animX = parsed.get("anim_x").getAsInt();
        this.animY = parsed.get("anim_y").getAsInt();
        this.reticleAlpha = parsed.get("reticle_alpha").getAsInt();
        this.reticleRendered = parsed.get("reticle_rendered").getAsBoolean();

        this.hb = new HitboxState(parsed.get("hb").getAsString());
        this.healthHb = new HitboxState(parsed.get("health_hb").getAsString());

        this.powers = Stream.of(parsed.get("powers").getAsString().split(POWER_DELIMETER))
                            .filter(s -> !s.isEmpty())
                            .map(PowerState::forJsonString)
                            .collect(Collectors.toCollection(ArrayList::new));
    }

    public void loadCreature(AbstractCreature creature) {
        creature.name = this.name;
        creature.id = this.id;
        creature.powers = this.powers.stream().map(powerState -> powerState.loadPower(creature))
                                     .collect(Collectors.toCollection(ArrayList::new));
        creature.isPlayer = this.isPlayer;
        creature.isBloodied = this.isBloodied;
        creature.drawX = this.drawX;
        creature.drawY = this.drawY;
        creature.dialogX = this.dialogX;
        creature.dialogY = this.dialogY;

        creature.hb = hb.loadHitbox();
        creature.healthHb = this.healthHb.loadHitbox();

        creature.gold = this.gold;
        creature.displayGold = this.displayGold;
        creature.isDying = this.isDying;
        creature.isDead = this.isDead;
        creature.halfDead = this.halfDead;
        creature.flipHorizontal = this.flipHorizontal;
        creature.flipVertical = this.flipVertical;
        creature.escapeTimer = this.escapeTimer;
        creature.isEscaping = this.isEscaping;
        creature.lastDamageTaken = this.lastDamageTaken;
        creature.hb_x = this.hb_x;
        creature.hb_y = this.hb_y;
        creature.hb_w = this.hb_w;
        creature.currentHealth = this.currentHealth;
        creature.maxHealth = this.maxHealth;
        creature.currentBlock = this.currentBlock;
        creature.hbAlpha = this.hbAlpha;
        creature.animX = this.animX;
        creature.animY = this.animY;
        creature.reticleAlpha = this.reticleAlpha;
        creature.reticleRendered = this.reticleRendered;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public String encode() {
        JsonObject creatureStateJson = new JsonObject();

        creatureStateJson.addProperty("name", name);
        creatureStateJson.addProperty("id", id);
        creatureStateJson.addProperty("is_player", isPlayer);
        creatureStateJson.addProperty("is_bloodied", isBloodied);
        creatureStateJson.addProperty("draw_x", drawX);
        creatureStateJson.addProperty("draw_y", drawY);
        creatureStateJson.addProperty("dialog_x", dialogX);
        creatureStateJson.addProperty("dialog_y", dialogY);
        creatureStateJson.addProperty("gold", gold);
        creatureStateJson.addProperty("display_gold", displayGold);
        creatureStateJson.addProperty("is_dying", isDying);
        creatureStateJson.addProperty("is_dead", isDead);
        creatureStateJson.addProperty("half_dead", halfDead);
        creatureStateJson.addProperty("flip_horizontal", flipHorizontal);
        creatureStateJson.addProperty("flip_vertical", flipVertical);
        creatureStateJson.addProperty("escape_timer", escapeTimer);
        creatureStateJson.addProperty("is_escaping", isEscaping);
        creatureStateJson.addProperty("last_damage_taken", lastDamageTaken);
        creatureStateJson.addProperty("hb_x", hb_x);
        creatureStateJson.addProperty("hb_y", hb_y);
        creatureStateJson.addProperty("hb_w", hb_w);
        creatureStateJson.addProperty("hb_h", hb_h);
        creatureStateJson.addProperty("current_health", currentHealth);
        creatureStateJson.addProperty("max_health", maxHealth);
        creatureStateJson.addProperty("current_block", currentBlock);
        creatureStateJson.addProperty("hb_alpha", hbAlpha);
        creatureStateJson.addProperty("anim_x", animX);
        creatureStateJson.addProperty("anim_y", animY);
        creatureStateJson.addProperty("reticle_alpha", reticleAlpha);
        creatureStateJson.addProperty("reticle_rendered", reticleRendered);

        creatureStateJson.addProperty("hb", hb.encode());
        creatureStateJson.addProperty("health_hb", healthHb.encode());

        creatureStateJson.addProperty("powers", powers.stream().map(PowerState::encode)
                                                      .collect(Collectors
                                                              .joining(POWER_DELIMETER)));

        return creatureStateJson.toString();
    }
}
