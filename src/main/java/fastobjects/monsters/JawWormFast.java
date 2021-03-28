package fastobjects.monsters;

import com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.exordium.JawWorm;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.combat.BlockedWordEffect;
import com.megacrit.cardcrawl.vfx.combat.DeckPoofEffect;
import com.megacrit.cardcrawl.vfx.combat.HbBlockBrokenEffect;
import com.megacrit.cardcrawl.vfx.combat.StrikeEffect;
import fastobjects.actions.DamageActionFast;
import fastobjects.actions.GainBlockActionFast;
import org.apache.logging.log4j.LogManager;

import java.util.Iterator;

public class JawWormFast extends JawWorm {
    public static final String ID = "JawWorm";
    public static final String NAME;
    public static final String[] MOVES;
    public static final String[] DIALOG;
    private static final MonsterStrings monsterStrings;
    private static final int HP_MIN = 40;
    private static final int HP_MAX = 44;
    private static final int A_2_HP_MIN = 42;
    private static final int A_2_HP_MAX = 46;
    private static final float HB_X = 0.0F;
    private static final float HB_Y = -25.0F;
    private static final float HB_W = 260.0F;
    private static final float HB_H = 170.0F;
    private static final int CHOMP_DMG = 11;
    private static final int A_2_CHOMP_DMG = 12;
    private static final int THRASH_DMG = 7;
    private static final int THRASH_BLOCK = 5;
    private static final int BELLOW_STR = 3;
    private static final int A_2_BELLOW_STR = 4;
    private static final int A_17_BELLOW_STR = 5;
    private static final int BELLOW_BLOCK = 6;
    private static final int A_17_BELLOW_BLOCK = 9;
    private static final byte CHOMP = 1;
    private static final byte BELLOW = 2;
    private static final byte THRASH = 3;

    static {
        monsterStrings = CardCrawlGame.languagePack.getMonsterStrings("JawWorm");
        NAME = monsterStrings.NAME;
        MOVES = monsterStrings.MOVES;
        DIALOG = monsterStrings.DIALOG;
    }

    private int bellowBlock;
    private int chompDmg;
    private int thrashDmg;
    private int thrashBlock;
    private int bellowStr;
    private boolean firstMove;
    private boolean hardMode;

    public JawWormFast(float x, float y) {
        this(x, y, false);
    }

    public JawWormFast(float x, float y, boolean hard) {
        super(x, y, hard);
    }

    public void usePreBattleAction() {
        if (this.hardMode) {
            AbstractDungeon.actionManager
                    .addToBottom(new ApplyPowerAction(this, this, new StrengthPower(this, this.bellowStr), this.bellowStr));
            AbstractDungeon.actionManager
                    .addToBottom(new GainBlockActionFast(this, this, this.bellowBlock));
        }

    }

    public void takeTurn() {
        LogManager.getLogger().info("Jaw Worm taking turn");
        switch (this.nextMove) {
            case 1:
                AbstractDungeon.actionManager
                        .addToBottom(new DamageActionFast(AbstractDungeon.player, this.damage
                                .get(0), AttackEffect.NONE));
                break;
            case 2:
                AbstractDungeon.actionManager
                        .addToBottom(new ApplyPowerAction(this, this, new StrengthPower(this, this.bellowStr), this.bellowStr, true, AttackEffect.NONE));
                AbstractDungeon.actionManager
                        .addToBottom(new GainBlockActionFast(this, this.bellowBlock, true));
                break;
            case 3:
                AbstractDungeon.actionManager
                        .addToBottom(new DamageActionFast(AbstractDungeon.player, this.damage
                                .get(1), AttackEffect.BLUNT_LIGHT));
                AbstractDungeon.actionManager
                        .addToBottom(new GainBlockActionFast(this, this.thrashBlock, true));
        }

        AbstractDungeon.actionManager.addToBottom(new RollMoveAction(this));
    }

    protected void getMove(int num) {
        if (this.firstMove) {
            this.firstMove = false;
            this.setMove((byte) 1, Intent.ATTACK, this.damage.get(0).base);
        } else {
            if (num < 25) {
                if (this.lastMove((byte) 1)) {
                    if (AbstractDungeon.aiRng.randomBoolean(0.5625F)) {
                        this.setMove(MOVES[0], (byte) 2, Intent.DEFEND_BUFF);
                    } else {
                        this.setMove((byte) 3, Intent.ATTACK_DEFEND, this.damage
                                .get(1).base);
                    }
                } else {
                    this.setMove((byte) 1, Intent.ATTACK, this.damage.get(0).base);
                }
            } else if (num < 55) {
                if (this.lastTwoMoves((byte) 3)) {
                    if (AbstractDungeon.aiRng.randomBoolean(0.357F)) {
                        this.setMove((byte) 1, Intent.ATTACK, this.damage
                                .get(0).base);
                    } else {
                        this.setMove(MOVES[0], (byte) 2, Intent.DEFEND_BUFF);
                    }
                } else {
                    this.setMove((byte) 3, Intent.ATTACK_DEFEND, this.damage
                            .get(1).base);
                }
            } else if (this.lastMove((byte) 2)) {
                if (AbstractDungeon.aiRng.randomBoolean(0.416F)) {
                    this.setMove((byte) 1, Intent.ATTACK, this.damage.get(0).base);
                } else {
                    this.setMove((byte) 3, Intent.ATTACK_DEFEND, this.damage
                            .get(1).base);
                }
            } else {
                this.setMove(MOVES[0], (byte) 2, Intent.DEFEND_BUFF);
            }

        }
    }

    public void die() {
        super.die();
        deathTimer = .00001F;
    }

    @Override
    public void damage(DamageInfo info) {
        if (info.output > 0 && this.hasPower("IntangiblePlayer")) {
            info.output = 1;
        }

        int damageAmount = info.output;
        if (!this.isDying && !this.isEscaping) {
            if (damageAmount < 0) {
                damageAmount = 0;
            }

            boolean hadBlock = true;
            if (this.currentBlock == 0) {
                hadBlock = false;
            }

            boolean weakenedToZero = damageAmount == 0;
            damageAmount = this.decrementBlock(info, damageAmount);
            Iterator var5;
            AbstractRelic r;
            if (info.owner == AbstractDungeon.player) {
                for (var5 = AbstractDungeon.player.relics.iterator(); var5
                        .hasNext(); damageAmount = r.onAttackToChangeDamage(info, damageAmount)) {
                    r = (AbstractRelic) var5.next();
                }
            }

            AbstractPower p;
            if (info.owner != null) {
                for (var5 = info.owner.powers.iterator(); var5.hasNext(); damageAmount = p
                        .onAttackToChangeDamage(info, damageAmount)) {
                    p = (AbstractPower) var5.next();
                }
            }

            for (var5 = this.powers.iterator(); var5.hasNext(); damageAmount = p
                    .onAttackedToChangeDamage(info, damageAmount)) {
                p = (AbstractPower) var5.next();
            }

            if (info.owner == AbstractDungeon.player) {
                var5 = AbstractDungeon.player.relics.iterator();

                while (var5.hasNext()) {
                    r = (AbstractRelic) var5.next();
                    r.onAttack(info, damageAmount, this);
                }
            }

            var5 = this.powers.iterator();

            while (var5.hasNext()) {
                p = (AbstractPower) var5.next();
                p.wasHPLost(info, damageAmount);
            }

            if (info.owner != null) {
                var5 = info.owner.powers.iterator();

                while (var5.hasNext()) {
                    p = (AbstractPower) var5.next();
                    p.onAttack(info, damageAmount, this);
                }
            }

            for (var5 = this.powers.iterator(); var5.hasNext(); damageAmount = p
                    .onAttacked(info, damageAmount)) {
                p = (AbstractPower) var5.next();
            }

            this.lastDamageTaken = Math.min(damageAmount, this.currentHealth);
            boolean probablyInstantKill = this.currentHealth == 0;
            if (damageAmount > 0) {
                if (info.owner != this) {
//                    this.useStaggerAnimation();
                }

                if (damageAmount >= 99 && !CardCrawlGame.overkill) {
                    CardCrawlGame.overkill = true;
                }

                this.currentHealth -= damageAmount;
                if (!probablyInstantKill) {
                    AbstractDungeon.effectList
                            .add(new StrikeEffect(this, this.hb.cX, this.hb.cY, damageAmount));
                }

                if (this.currentHealth < 0) {
                    this.currentHealth = 0;
                }

                this.healthBarUpdatedEvent();
            } else if (!probablyInstantKill) {
                if (weakenedToZero && this.currentBlock == 0) {
                    if (hadBlock) {
                        AbstractDungeon.effectList
                                .add(new BlockedWordEffect(this, this.hb.cX, this.hb.cY, TEXT[30]));
                    } else {
                        AbstractDungeon.effectList
                                .add(new StrikeEffect(this, this.hb.cX, this.hb.cY, 0));
                    }
                } else if (Settings.SHOW_DMG_BLOCK) {
                    AbstractDungeon.effectList
                            .add(new BlockedWordEffect(this, this.hb.cX, this.hb.cY, TEXT[30]));
                }
            }

            if (this.currentHealth <= 0) {
                this.die();
                if (AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
                    AbstractDungeon.actionManager.cleanCardQueue();
                    AbstractDungeon.effectList
                            .add(new DeckPoofEffect(64.0F * Settings.scale, 64.0F * Settings.scale, true));
                    AbstractDungeon.effectList
                            .add(new DeckPoofEffect((float) Settings.WIDTH - 64.0F * Settings.scale, 64.0F * Settings.scale, false));
                    AbstractDungeon.overlayMenu.hideCombatPanels();
                }

                if (this.currentBlock > 0) {
                    this.loseBlock();
                    AbstractDungeon.effectList
                            .add(new HbBlockBrokenEffect(this.hb.cX - this.hb.width / 2.0F + BLOCK_ICON_X, this.hb.cY - this.hb.height / 2.0F + BLOCK_ICON_Y));
                }
            }

        }
    }

    @Override
    public void useFastShakeAnimation(float duration) {
        // no shaking, fast or otherwise
    }
}
