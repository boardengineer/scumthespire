package savestate;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.monsters.exordium.*;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MonsterState extends CreatureState {
    private final float deathTimer;
    private final boolean tintFadeOutCalled;
    private final boolean escaped;
    private final boolean escapeNext;
    private final AbstractMonster.EnemyType type;
    private final boolean cannotEscape;
    private final ArrayList<DamageInfoState> damage;
    private final ArrayList<Byte> moveHistory;
    private final byte nextMove;
    private final Hitbox intentHb;
    private final AbstractMonster.Intent intent;
    private final AbstractMonster.Intent tipIntent;
    private final float intentAlpha;
    private final float intentAlphaTarget;
    private final float intentOffsetX;
    private final String moveName;
    private final EnemyMoveInfoState moveInfo;

    public MonsterState(AbstractMonster monster) {
        super(monster);

        this.moveInfo = new EnemyMoveInfoState((EnemyMoveInfo) ReflectionHacks
                .getPrivate(monster, AbstractMonster.class, "move"));

        this.deathTimer = monster.deathTimer;
        this.tintFadeOutCalled = monster.tintFadeOutCalled;
        this.escaped = monster.escaped;
        this.escapeNext = monster.escapeNext;
        this.type = monster.type;
        this.cannotEscape = monster.cannotEscape;
        this.damage = monster.damage.stream().map(DamageInfoState::new)
                                    .collect(Collectors.toCollection(ArrayList::new));
        this.moveHistory = monster.moveHistory.stream().map(Byte::byteValue)
                                              .collect(Collectors.toCollection(ArrayList::new));

        this.nextMove = monster.nextMove;
        this.intentHb = monster.intentHb;
        this.intent = monster.intent;
        this.tipIntent = monster.tipIntent;
        this.intentAlpha = monster.intentAlpha;
        this.intentAlphaTarget = monster.intentAlphaTarget;
        this.intentOffsetX = monster.intentOffsetX;
        this.moveName = monster.moveName;
    }

    public AbstractMonster loadMonster() {
        AbstractMonster monster = getMonsterFromId();
        super.loadCreature(monster);
        monster.init();


        monster.deathTimer = this.deathTimer;
        monster.tintFadeOutCalled = this.tintFadeOutCalled;
        monster.escaped = this.escaped;
        monster.escapeNext = this.escapeNext;
        monster.type = this.type;
        monster.cannotEscape = this.cannotEscape;
        monster.damage = this.damage.stream().map(DamageInfoState::loadDamageInfo)
                                    .collect(Collectors.toCollection(ArrayList::new));

        monster.setMove(moveName, moveInfo.nextMove, moveInfo.intent, moveInfo.baseDamage, moveInfo.multiplier, moveInfo.isMultiDamage);

        monster.moveHistory = this.moveHistory.stream()
                                              .collect(Collectors.toCollection(ArrayList::new));

        monster.nextMove = this.nextMove;
        monster.intentHb = this.intentHb;
        monster.intent = this.intent;
        monster.tipIntent = this.tipIntent;
        monster.intentAlpha = this.intentAlpha;
        monster.intentAlphaTarget = this.intentAlphaTarget;
        monster.intentOffsetX = this.intentOffsetX;
        monster.moveName = this.moveName;


//        monster.tint = new TintEffect();
//        monster.healthBarUpdatedEvent();
//        monster.showHealthBar();
//        monster.update();
        monster.createIntent();
//        monster.updatePowers();

        return monster;
    }

    private AbstractMonster getMonsterFromId() {
        float offsetX = (drawX - (float) Settings.WIDTH * 0.75F) / Settings.xScale;
        float offsetY = (drawY - AbstractDungeon.floorY) / Settings.yScale;

        AbstractMonster monster = null;
        // exordium fastobjects.monsters
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
            if (intent != AbstractMonster.Intent.BUFF) {
                // clear the firstMove boolean by rolling a move
                monster.rollMove();
            }
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
            monster = new JawWorm(offsetX, offsetY);
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
        } else {
            System.err.println("couldn't find monster with id " + id);
        }

        return monster;
    }
}
