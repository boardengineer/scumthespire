package battleaimod.patches;

import basemod.ReflectionHacks;
import battleaimod.BattleAiMod;
import battleaimod.fastobjects.AnimationStateFast;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.animations.AnimateHopAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.animations.SetAnimationAction;
import com.megacrit.cardcrawl.actions.common.EscapeAction;
import com.megacrit.cardcrawl.actions.common.SpawnMonsterAction;
import com.megacrit.cardcrawl.actions.unique.SummonGremlinAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.PommelStrike;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.cardManip.ExhaustCardEffect;

import java.util.Iterator;


public class MonsterPatch {
    public static boolean shouldGoFast() {
        return BattleAiMod.goFast;
//        return true;
    }

    @SpirePatch(
            clz = AbstractMonster.class,
            paramtypez = {},
            method = "die"
    )
    public static class MonsterDeathPatch {
        public static void Postfix(AbstractMonster _instance) {
            if (shouldGoFast()) {
                _instance.deathTimer = .0F;
                _instance.isDying = true;
                _instance.isDead = true;
                _instance.dispose();
            }
        }
    }

    @SpirePatch(
            clz = AbstractMonster.class,
            paramtypez = {boolean.class},
            method = "die"
    )
    public static class MonsterDeathWithRelicsPatch {
        public static SpireReturn Prefix(AbstractMonster monster, boolean triggerRelics) {
            if (shouldGoFast()) {

                if (!monster.isDying) {
                    monster.isDying = true;
                    Iterator var2;
                    if (monster.currentHealth <= 0 && triggerRelics) {
                        var2 = monster.powers.iterator();

                        while(var2.hasNext()) {
                            AbstractPower p = (AbstractPower)var2.next();
                            p.onDeath();
                        }
                    }

                    if (triggerRelics) {
                        var2 = AbstractDungeon.player.relics.iterator();

                        while(var2.hasNext()) {
                            AbstractRelic r = (AbstractRelic)var2.next();
                            r.onMonsterDeath(monster);
                        }
                    }

                    if (AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
                        AbstractDungeon.overlayMenu.endTurnButton.disable();
                        var2 = AbstractDungeon.player.limbo.group.iterator();

                        while(var2.hasNext()) {
                            AbstractCard c = (AbstractCard)var2.next();
                            AbstractDungeon.effectList.add(new ExhaustCardEffect(c));
                        }

                        AbstractDungeon.player.limbo.clear();
                    }

                    if (monster.currentHealth < 0) {
                        monster.currentHealth = 0;
                    }
                }

                return SpireReturn.Return(null);
            }

            return SpireReturn.Continue();
        }

        public static void Postfix(AbstractMonster _instance, boolean triggerRelics) {
            if (shouldGoFast()) {
                _instance.deathTimer = .0F;
                _instance.isDying = true;
                _instance.isDead = true;
                _instance.dispose();
            }
        }
    }

    @SpirePatch(
            clz = AbstractMonster.class,
            paramtypez = {},
            method = "onBossVictoryLogic"
    )
    public static class BossFastDeathPatch {
        public static void Postfix(AbstractMonster _instance) {
            if (shouldGoFast()) {
                _instance.deathTimer = .0000001F;
            }
        }
    }

    @SpirePatch(
            clz = SetAnimationAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class SetAnimationPatch {
        public static void Prefix(SetAnimationAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = AbstractCreature.class,
            paramtypez = {},
            method = "updateAnimations"
    )
    public static class NoUpdateCreatureAnimationsPlayerPatch {
        public static SpireReturn Prefix(AbstractCreature _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = EscapeAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FastEscapePatch {
        public static SpireReturn Prefix(EscapeAction _instance) {
            if (shouldGoFast()) {
                AbstractMonster m = (AbstractMonster) _instance.source;
                m.escape();
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AnimateHopAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class HopAnimationPatch {
        public static void Prefix(AnimateHopAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = AnimateSlowAttackAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class SlowAttackAnimationPatch {
        public static void Prefix(AnimateSlowAttackAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = SpawnMonsterAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class SpawnMonsterAnimationPatch {
        public static void Prefix(SpawnMonsterAction _instance) {
            ReflectionHacks.setPrivate(_instance, SpawnMonsterAction.class, "targetSlot", -99);
            ReflectionHacks
                    .setPrivate(_instance, SpawnMonsterAction.class, "useSmartPositioning", false);

            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = SummonGremlinAction.class,
            paramtypez = {},
            method = "getSmartPosition"
    )
    public static class SummonGremlinActionPatch {
        PommelStrike power;

        public static SpireReturn Prefix(SummonGremlinAction _instance) {
            return SpireReturn.Return(0);
        }
    }

    @SpirePatch(
            clz = AnimateFastAttackAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class FastAttackAnimationPatch {
        public static void Prefix(AnimateFastAttackAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
            }
        }
    }

    @SpirePatch(
            clz = AbstractMonster.class,
            paramtypez = {},
            method = "createIntent"
    )
    public static class NoShowInetntPatch {
        public static SpireReturn Prefix(AbstractMonster _instance) {
            if (shouldGoFast()) {

                EnemyMoveInfo move = ReflectionHacks
                        .getPrivate(_instance, AbstractMonster.class, "move");
                _instance.nextMove = move.nextMove;
                _instance.setIntentBaseDmg(move.baseDamage);
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractMonster.class,
            paramtypez = {},
            method = "updateEscapeAnimation"
    )
    public static class NoAnimationsPatch {
        public static SpireReturn Prefix(AbstractMonster _instance) {
            if (shouldGoFast()) {
                if (_instance.escapeTimer != 0) {
                    _instance.escaped = true;
                    _instance.escapeTimer = -.5F;
                    if (AbstractDungeon.getMonsters().areMonstersDead() && !AbstractDungeon
                            .getCurrRoom().isBattleOver && !AbstractDungeon
                            .getCurrRoom().cannotLose) {
                        AbstractDungeon.getCurrRoom().endBattle();
                    }
                }

                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
