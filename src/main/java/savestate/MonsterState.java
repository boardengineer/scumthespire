package savestate;

import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.*;
import com.megacrit.cardcrawl.vfx.TintEffect;
import communicationmod.CreatureLoader;

import java.util.ArrayList;

public class MonsterState extends CreatureLoader {
    private final float deathTimer;
    private final boolean tintFadeOutCalled;
    private final boolean escaped;
    private final boolean escapeNext;
    private final AbstractMonster.EnemyType type;
    private final boolean cannotEscape;
    private final ArrayList<DamageInfo> damage;
    private final ArrayList<Byte> moveHistory;
    private final byte nextMove;
    private final Hitbox intentHb;
    private final AbstractMonster.Intent intent;
    private final AbstractMonster.Intent tipIntent;
    private final float intentAlpha;
    private final float intentAlphaTarget;
    private final float intentOffsetX;
    private final String moveName;
    private final AbstractMonster monster;

    public MonsterState(AbstractMonster monster) {
        super(monster);

        this.deathTimer = monster.deathTimer;
        this.tintFadeOutCalled = monster.tintFadeOutCalled;
        this.escaped = monster.escaped;
        this.escapeNext = monster.escapeNext;
        this.type = monster.type;
        this.cannotEscape = monster.cannotEscape;
        this.damage = (ArrayList<DamageInfo>) monster.damage.clone();
        this.moveHistory = (ArrayList<Byte>) monster.moveHistory.clone();
        this.nextMove = monster.nextMove;
        this.intentHb = monster.intentHb;
        this.intent = monster.intent;
        this.tipIntent = monster.tipIntent;
        this.intentAlpha = monster.intentAlpha;
        this.intentAlphaTarget = monster.intentAlphaTarget;
        this.intentOffsetX = monster.intentOffsetX;
        this.moveName = monster.moveName;
        this.monster = monster;
    }

    public AbstractMonster loadMonster() {
        AbstractMonster monster = resetMonster();
        super.loadCreature(monster);

        monster.deathTimer = this.deathTimer;
        monster.tintFadeOutCalled = this.tintFadeOutCalled;
        monster.escaped = this.escaped;
        monster.escapeNext = this.escapeNext;
        monster.type = this.type;
        monster.cannotEscape = this.cannotEscape;
        monster.damage = (ArrayList<DamageInfo>) this.damage.clone();
        monster.moveHistory = (ArrayList<Byte>) this.moveHistory.clone();
        monster.nextMove = this.nextMove;
        monster.intentHb = this.intentHb;
        monster.intent = this.intent;
        monster.tipIntent = this.tipIntent;
        monster.intentAlpha = this.intentAlpha;
        monster.intentAlphaTarget = this.intentAlphaTarget;
        monster.intentOffsetX = this.intentOffsetX;
        monster.moveName = this.moveName;

        monster.tint = new TintEffect();
        monster.init();
        monster.healthBarUpdatedEvent();
        monster.showHealthBar();
        monster.update();

        System.out.printf("next move:%s\n", nextMove);
        System.out.printf("intent:%s\n", intent);

        return monster;
    }

    private AbstractMonster resetMonster() {
        AbstractMonster monster = this.monster;
        float offsetX = (monster.drawX - (float) Settings.WIDTH * 0.75F) / Settings.xScale;
        float offsetY = (monster.drawY - AbstractDungeon.floorY) / Settings.yScale;

        // exordium monsters
        if (monster instanceof AcidSlime_L) {
            monster = new AcidSlime_L(offsetX, offsetY);
        } else if (monster instanceof AcidSlime_M) {
            monster = new AcidSlime_M(offsetX, offsetY);
        } else if (monster instanceof AcidSlime_S) {
            monster = new AcidSlime_S(offsetX, offsetY, 0);
        } else if (monster instanceof ApologySlime) {
            monster = new ApologySlime();
        } else if (monster instanceof Cultist) {
            monster = new Cultist(offsetX, offsetY);
            if (intent != AbstractMonster.Intent.BUFF) {
                // clear the firstMove boolean by rolling a move
                monster.rollMove();
            }
        } else if (monster instanceof FungiBeast) {
            monster = new FungiBeast(offsetX, offsetY);
        } else if (monster instanceof GremlinFat) {
            monster = new GremlinFat(offsetX, offsetY);
        } else if (monster instanceof GremlinNob) {
            monster = new GremlinNob(offsetX, offsetY);
        } else if (monster instanceof GremlinThief) {
            monster = new GremlinThief(offsetX, offsetY);
        } else if (monster instanceof GremlinTsundere) {
            monster = new GremlinTsundere(offsetX, offsetY);
        } else if (monster instanceof GremlinWarrior) {
            monster = new GremlinWarrior(offsetX, offsetY);
        } else if (monster instanceof GremlinWizard) {
            monster = new GremlinWizard(offsetX, offsetY);
        } else if (monster instanceof Hexaghost) {
            monster = new Hexaghost();
        } else if (monster instanceof JawWorm) {
            monster = new JawWorm(offsetX, offsetY);
            if (!monster.moveHistory.isEmpty()) {
                monster.rollMove();
            }
        } else if (monster instanceof Lagavulin) {
            monster = new Lagavulin(false);
        } else if (monster instanceof Looter) {
            monster = new Looter(offsetX, offsetY);
        } else if (monster instanceof LouseDefensive) {
            monster = new LouseDefensive(offsetX, offsetY);
        } else if (monster instanceof LouseNormal) {
            monster = new LouseNormal(offsetX, offsetY);
        } else if (monster instanceof Sentry) {
            monster = new Sentry(offsetX, offsetY);
        } else if (monster instanceof SlaverBlue) {
            monster = new SlaverBlue(offsetX, offsetY);
        } else if (monster instanceof SlaverRed) {
            monster = new SlaverRed(offsetX, offsetY);
        } else if (monster instanceof SlimeBoss) {
            monster = new SlimeBoss();
        } else if (monster instanceof SpikeSlime_L) {
            monster = new SpikeSlime_L(offsetX, offsetY);
        } else if (monster instanceof SpikeSlime_M) {
            monster = new SpikeSlime_M(offsetX, offsetY);
        } else if (monster instanceof SpikeSlime_S) {
            monster = new SpikeSlime_S(offsetX, offsetY, 0);
        } else if (monster instanceof TheGuardian) {
            monster = new TheGuardian();
        }

        return monster;
    }
}
