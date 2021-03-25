package communicationmod;

import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.vfx.TintEffect;

import java.util.ArrayList;

public class CreatureLoader {
    private final String name;
    private final String id;
    private final ArrayList<AbstractPower> powers;
    private final boolean isPlayer;
    private final boolean isBloodied;
    private final float drawX;
    private final float drawY;
    private final float dialogX;
    private final float dialogY;
    private final HitboxLoader hb;
    private final int gold;
    private final int displayGold;
    private final boolean isDying;
    private final boolean isDead;
    private final boolean halfDead;
    private final boolean flipHorizontal;
    private final boolean flipVertical;
    private final float escapeTimer;
    private final boolean isEscaping;
    private final Hitbox healthHb;
    private final int lastDamageTaken;
    private final float hb_x;
    private final float hb_y;
    private final float hb_w;
    private final float hb_h;
    private final int currentHealth;
    private final int maxHealth;
    private final int currentBlock;
    private final float hbAlpha;
    private final TintEffect tint;
    private final float animX;
    private final float animY;
    private final AnimationState state;
    private final float reticleAlpha;
    private final boolean reticleRendered;

    public CreatureLoader(AbstractCreature creature) {
        this.name = creature.name;
        this.id = creature.id;
        this.powers = (ArrayList<AbstractPower>) creature.powers.clone();
        this.isPlayer = creature.isPlayer;
        this.isBloodied = creature.isBloodied;
        this.drawX = creature.drawX;
        this.drawY = creature.drawY;
        this.dialogX = creature.dialogX;
        this.dialogY = creature.dialogY;

        this.hb = new HitboxLoader(creature.hb);

        this.gold = creature.gold;
        this.displayGold = creature.displayGold;
        this.isDying = creature.isDying;
        this.isDead = creature.isDead;
        this.halfDead = creature.halfDead;
        this.flipHorizontal = creature.flipHorizontal;
        this.flipVertical = creature.flipVertical;
        this.escapeTimer = creature.escapeTimer;
        this.isEscaping = creature.isEscaping;
        this.healthHb = creature.healthHb;
        this.lastDamageTaken = creature.lastDamageTaken;
        this.hb_x = creature.hb_x;
        this.hb_y = creature.hb_y;
        this.hb_w = creature.hb_w;
        this.hb_h = creature.hb_h;
        this.currentHealth = creature.currentHealth;
        this.maxHealth = creature.maxHealth;
        this.currentBlock = creature.currentBlock;
        this.hbAlpha = creature.hbAlpha;
        this.tint = creature.tint;
        this.animX = creature.animX;
        this.animY = creature.animY;
        this.state = creature.state;
        this.reticleAlpha = creature.reticleAlpha;
        this.reticleRendered = creature.reticleRendered;
    }

    public void loadCreature(AbstractCreature creature) {
        creature.name = this.name;
        creature.id = this.id;
        creature.powers = (ArrayList<AbstractPower>) this.powers.clone();
        creature.isPlayer = this.isPlayer;
        creature.isBloodied = this.isBloodied;
        creature.drawX = this.drawX;
        creature.drawY = this.drawY;
        creature.dialogX = this.dialogX;
        creature.dialogY = this.dialogY;
        creature.hb = hb.loadHitbox();
        hb.loadHitbox();
        creature.gold = this.gold;
        creature.displayGold = this.displayGold;
        creature.isDying = this.isDying;
        creature.isDead = this.isDead;
        creature.halfDead = this.halfDead;
        creature.flipHorizontal = this.flipHorizontal;
        creature.flipVertical = this.flipVertical;
        creature.escapeTimer = this.escapeTimer;
        creature.isEscaping = this.isEscaping;
        creature.healthHb = this.healthHb;
        creature.lastDamageTaken = this.lastDamageTaken;
        creature.hb_x = this.hb_x;
        creature.hb_y = this.hb_y;
        creature.hb_w = this.hb_w;
        creature.currentHealth = this.currentHealth;
        creature.maxHealth = this.maxHealth;
        creature.currentBlock = this.currentBlock;
        creature.hbAlpha = this.hbAlpha;
        creature.tint = this.tint;
        creature.animX = this.animX;
        creature.animY = this.animY;
        creature.state = this.state;
        creature.reticleAlpha = this.reticleAlpha;
        creature.reticleRendered = this.reticleRendered;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }
}
