package battleaimod.patches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.combat.ShockWaveEffect;
import com.megacrit.cardcrawl.vfx.combat.SmallLaserEffect;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class FXPatches {
    @SpirePatch(
            clz = VFXAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoFxUpdatePatch {
        public static SpireReturn Prefix(VFXAction _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = VFXAction.class,
            paramtypez = {AbstractCreature.class, AbstractGameEffect.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoFxConstructorPatch {
        public static SpireReturn Prefix(VFXAction _instance, AbstractCreature source, AbstractGameEffect effect, float duration) {
            if (shouldGoFast()) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = VFXAction.class,
            paramtypez = {AbstractCreature.class, AbstractGameEffect.class, float.class, boolean.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoFxConstructorPatchOther {
        public static SpireReturn Prefix(VFXAction _instance, AbstractCreature source, AbstractGameEffect effect, float duration, boolean topLevel) {
            if (shouldGoFast()) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = SmallLaserEffect.class,
            paramtypez = {float.class, float.class, float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoLaserFxConstructorPatch {
        public static SpireReturn Prefix(SmallLaserEffect _instance, float sX, float sY, float dX, float dY) {
            if (shouldGoFast()) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = SmallLaserEffect.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoLaserFxUpdatePatch {
        public static SpireReturn Prefix(SmallLaserEffect _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = SmallLaserEffect.class,
            paramtypez = {SpriteBatch.class},
            method = "render"
    )
    public static class NoLaserFxRenderPatch {
        public static SpireReturn Prefix(SmallLaserEffect _instance, SpriteBatch sb) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = ShockWaveEffect.class,
            paramtypez = {float.class, float.class, Color.class, ShockWaveEffect.ShockWaveType.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoShockwaveFxConstructorPatch {
        public static SpireReturn Prefix(ShockWaveEffect _instance, float x, float y, Color color, ShockWaveEffect.ShockWaveType type) {
            if (shouldGoFast()) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = ShockWaveEffect.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoShockwaveFxUpdatePatch {
        public static SpireReturn Prefix(ShockWaveEffect _instance) {
            if (shouldGoFast()) {
                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
