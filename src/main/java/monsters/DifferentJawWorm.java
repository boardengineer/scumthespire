//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package monsters;

import com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.exordium.JawWorm;
import com.megacrit.cardcrawl.powers.StrengthPower;
import org.apache.logging.log4j.LogManager;

public class DifferentJawWorm extends JawWorm {
    public static final String ID = "JawWorm";
    private static final MonsterStrings monsterStrings;
    public static final String NAME;
    public static final String[] MOVES;
    public static final String[] DIALOG;
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
    private int bellowBlock;
    private int chompDmg;
    private int thrashDmg;
    private int thrashBlock;
    private int bellowStr;
    private static final byte CHOMP = 1;
    private static final byte BELLOW = 2;
    private static final byte THRASH = 3;
    private boolean firstMove;
    private boolean hardMode;

    public DifferentJawWorm(float x, float y) {
        this(x, y, false);
    }

    public DifferentJawWorm(float x, float y, boolean hard) {
        super(x,y,hard);
    }

    public void usePreBattleAction() {
        if (this.hardMode) {
            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new StrengthPower(this, this.bellowStr), this.bellowStr));
            AbstractDungeon.actionManager.addToBottom(new GainBlockAction(this, this, this.bellowBlock));
        }

    }

    public void takeTurn() {
        LogManager.getLogger().info("Jaw Worm taking turn");
        switch(this.nextMove) {
            case 1:
                AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, (DamageInfo)this.damage.get(0), AttackEffect.NONE));
                break;
            case 2:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new StrengthPower(this, this.bellowStr), this.bellowStr));
                AbstractDungeon.actionManager.addToBottom(new GainBlockAction(this, this, this.bellowBlock));
                break;
            case 3:
                AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, (DamageInfo)this.damage.get(1), AttackEffect.BLUNT_LIGHT));
                AbstractDungeon.actionManager.addToBottom(new GainBlockAction(this, this, this.thrashBlock));
        }

        AbstractDungeon.actionManager.addToBottom(new RollMoveAction(this));
    }

    protected void getMove(int num) {
        if (this.firstMove) {
            this.firstMove = false;
            this.setMove((byte)1, Intent.ATTACK, ((DamageInfo)this.damage.get(0)).base);
        } else {
            if (num < 25) {
                if (this.lastMove((byte)1)) {
                    if (AbstractDungeon.aiRng.randomBoolean(0.5625F)) {
                        this.setMove(MOVES[0], (byte)2, Intent.DEFEND_BUFF);
                    } else {
                        this.setMove((byte)3, Intent.ATTACK_DEFEND, ((DamageInfo)this.damage.get(1)).base);
                    }
                } else {
                    this.setMove((byte)1, Intent.ATTACK, ((DamageInfo)this.damage.get(0)).base);
                }
            } else if (num < 55) {
                if (this.lastTwoMoves((byte)3)) {
                    if (AbstractDungeon.aiRng.randomBoolean(0.357F)) {
                        this.setMove((byte)1, Intent.ATTACK, ((DamageInfo)this.damage.get(0)).base);
                    } else {
                        this.setMove(MOVES[0], (byte)2, Intent.DEFEND_BUFF);
                    }
                } else {
                    this.setMove((byte)3, Intent.ATTACK_DEFEND, ((DamageInfo)this.damage.get(1)).base);
                }
            } else if (this.lastMove((byte)2)) {
                if (AbstractDungeon.aiRng.randomBoolean(0.416F)) {
                    this.setMove((byte)1, Intent.ATTACK, ((DamageInfo)this.damage.get(0)).base);
                } else {
                    this.setMove((byte)3, Intent.ATTACK_DEFEND, ((DamageInfo)this.damage.get(1)).base);
                }
            } else {
                this.setMove(MOVES[0], (byte)2, Intent.DEFEND_BUFF);
            }

        }
    }

    public void die() {
        super.die();
        deathTimer = .00001F;
        CardCrawlGame.sound.play("JAW_WORM_DEATH");
    }

    static {
        monsterStrings = CardCrawlGame.languagePack.getMonsterStrings("JawWorm");
        NAME = monsterStrings.NAME;
        MOVES = monsterStrings.MOVES;
        DIALOG = monsterStrings.DIALOG;
    }
}
