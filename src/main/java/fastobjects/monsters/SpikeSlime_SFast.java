//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fastobjects.monsters;

import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState.TrackEntry;
import com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.SlimeAnimListener;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.exordium.SpikeSlime_S;
import com.megacrit.cardcrawl.powers.PoisonPower;

public class SpikeSlime_SFast extends SpikeSlime_S {
    public static final String ID = "SpikeSlime_S";
    private static final MonsterStrings monsterStrings;
    public static final String NAME;
    public static final String[] MOVES;
    public static final String[] DIALOG;
    public static final int HP_MIN = 10;
    public static final int HP_MAX = 14;
    public static final int A_2_HP_MIN = 11;
    public static final int A_2_HP_MAX = 15;
    public static final int TACKLE_DAMAGE = 5;
    public static final int A_2_TACKLE_DAMAGE = 6;
    private static final byte TACKLE = 1;

    public SpikeSlime_SFast(float x, float y, int poisonAmount) {
        super(x,y,poisonAmount);
        if (AbstractDungeon.ascensionLevel >= 7) {
            this.setHp(11, 15);
        } else {
            this.setHp(10, 14);
        }

        if (AbstractDungeon.ascensionLevel >= 2) {
            this.damage.add(new DamageInfo(this, 6));
        } else {
            this.damage.add(new DamageInfo(this, 5));
        }

        if (poisonAmount >= 1) {
            this.powers.add(new PoisonPower(this, this, poisonAmount));
        }

        this.loadAnimation("images/monsters/theBottom/slimeAltS/skeleton.atlas", "images/monsters/theBottom/slimeAltS/skeleton.json", 1.0F);
        TrackEntry e = this.state.setAnimation(0, "idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        this.state.addListener(new SlimeAnimListener());
    }

    public void takeTurn() {
        switch(this.nextMove) {
            case 1:
                AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, (DamageInfo)this.damage.get(0), AttackEffect.BLUNT_HEAVY));
                AbstractDungeon.actionManager.addToBottom(new RollMoveAction(this));
            default:
        }
    }

    @Override
    public void die() {
        super.die();
        deathTimer = .00001F;
    }

    protected void getMove(int num) {
        this.setMove((byte)1, Intent.ATTACK, ((DamageInfo)this.damage.get(0)).base);
    }

    static {
        monsterStrings = CardCrawlGame.languagePack.getMonsterStrings("SpikeSlime_S");
        NAME = monsterStrings.NAME;
        MOVES = monsterStrings.MOVES;
        DIALOG = monsterStrings.DIALOG;
    }
}
