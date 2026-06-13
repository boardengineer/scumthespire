package battleaimod.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.Soul;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;

public class FastActionPatches {

    private static final float WAIT_ACTION_MAX_DURATION = 0.0001f;
    private static final float VFX_ACTION_MAX_DURATION = 0.0002f;
    private static final float DRAW_ACTION_MAX_DURATION = 0.0001f;
    private static final float SHUFFLE_ACTION_MAX_DURATION = 0.0001f;
    private static final float EXHAUST_ACTION_MAX_DURATION = 0.0001f;
    private static final float BLOCK_ANIM_MAX_TIMER = 0.0001f;

    public static void enableFastMode() {
        Settings.FAST_MODE = true;
        Settings.ACTION_DUR_XFAST = 0.0001F;
        Settings.ACTION_DUR_FASTER = 0.0002F;
        Settings.ACTION_DUR_FAST = 0.00025F;
        Settings.ACTION_DUR_MED = 0.0005F;
        Settings.ACTION_DUR_LONG = 0.001F;
        Settings.ACTION_DUR_XLONG = 0.0015F;
        Settings.SCREEN_SHAKE = false;
        Settings.DISABLE_EFFECTS = true;
    }

    public static void disableFastMode() {
        //Settings.FAST_MODE = false;
    }

    private static void clampActionDuration(AbstractGameAction action, float maxDuration) {
        if (action == null) return;

        try {
            float duration = ReflectionHacks.getPrivate(
                    action,
                    AbstractGameAction.class,
                    "duration"
            );

            float startDuration = ReflectionHacks.getPrivate(
                    action,
                    AbstractGameAction.class,
                    "startDuration"
            );

            if (duration > maxDuration) {
                ReflectionHacks.setPrivate(
                        action,
                        AbstractGameAction.class,
                        "duration",
                        maxDuration
                );
            }

            if (startDuration > maxDuration) {
                ReflectionHacks.setPrivate(
                        action,
                        AbstractGameAction.class,
                        "startDuration",
                        maxDuration
                );
            }
        } catch (Exception ignored) {
        }
    }

    @SpirePatch(
            clz = WaitAction.class,
            method = SpirePatch.CONSTRUCTOR,
            paramtypez = { float.class }
    )
    public static class FastWaitActionPatch {
        public static void Postfix(WaitAction __instance) {
            clampActionDuration(__instance, WAIT_ACTION_MAX_DURATION);
        }
    }

    @SpirePatch(
            clz = VFXAction.class,
            method = SpirePatch.CONSTRUCTOR,
            paramtypez = {
                    com.megacrit.cardcrawl.vfx.AbstractGameEffect.class,
                    float.class
            }
    )
    public static class FastVFXActionPatch1 {
        public static void Postfix(VFXAction __instance) {
            clampActionDuration(__instance, VFX_ACTION_MAX_DURATION);
        }
    }

    @SpirePatch(
            clz = VFXAction.class,
            method = SpirePatch.CONSTRUCTOR,
            paramtypez = {
                    com.megacrit.cardcrawl.core.AbstractCreature.class,
                    com.megacrit.cardcrawl.vfx.AbstractGameEffect.class,
                    float.class
            }
    )
    public static class FastVFXActionPatch2 {
        public static void Postfix(VFXAction __instance) {
            clampActionDuration(__instance, VFX_ACTION_MAX_DURATION);
        }
    }

    @SpirePatch(
            clz = DrawCardAction.class,
            method = SpirePatch.CONSTRUCTOR,
            paramtypez = { int.class }
    )
    public static class FastDrawCardActionPatch1 {
        public static void Postfix(DrawCardAction __instance) {
            clampActionDuration(__instance, DRAW_ACTION_MAX_DURATION);
        }
    }

    @SpirePatch(
            clz = DrawCardAction.class,
            method = SpirePatch.CONSTRUCTOR,
            paramtypez = {
                    com.megacrit.cardcrawl.core.AbstractCreature.class,
                    int.class
            }
    )
    public static class FastDrawCardActionPatch2 {
        public static void Postfix(DrawCardAction __instance) {
            clampActionDuration(__instance, DRAW_ACTION_MAX_DURATION);
        }
    }

    @SpirePatch(
            clz = DrawCardAction.class,
            method = SpirePatch.CONSTRUCTOR,
            paramtypez = {
                    com.megacrit.cardcrawl.core.AbstractCreature.class,
                    int.class,
                    boolean.class
            }
    )
    public static class FastDrawCardActionPatch3 {
        public static void Postfix(DrawCardAction __instance) {
            clampActionDuration(__instance, DRAW_ACTION_MAX_DURATION);
        }
    }

    @SpirePatch(
            clz = EmptyDeckShuffleAction.class,
            method = SpirePatch.CONSTRUCTOR
    )
    public static class FastEmptyDeckShuffleActionPatch {
        public static void Postfix(EmptyDeckShuffleAction __instance) {
            clampActionDuration(__instance, SHUFFLE_ACTION_MAX_DURATION);
        }
    }



    @SpirePatch(
            clz = ExhaustSpecificCardAction.class,
            method = SpirePatch.CONSTRUCTOR,
            paramtypez = {
                    com.megacrit.cardcrawl.cards.AbstractCard.class,
                    com.megacrit.cardcrawl.cards.CardGroup.class
            }
    )
    public static class FastExhaustSpecificCardActionPatch {
        public static void Postfix(ExhaustSpecificCardAction __instance) {
            clampActionDuration(__instance, EXHAUST_ACTION_MAX_DURATION);
        }
    }

    @SpirePatch(
            clz = AbstractCreature.class,
            method = "updateBlockAnimations"
    )
    public static class FastBlockAnimationPatch {
        public static void Postfix(AbstractCreature __instance) {
            try {
                float blockAnimTimer = ReflectionHacks.getPrivate(
                        __instance,
                        AbstractCreature.class,
                        "blockAnimTimer"
                );

                if (blockAnimTimer > BLOCK_ANIM_MAX_TIMER) {
                    ReflectionHacks.setPrivate(
                            __instance,
                            AbstractCreature.class,
                            "blockAnimTimer",
                            BLOCK_ANIM_MAX_TIMER
                    );
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static final float SOUL_MIN_SPEED = 6000.0f;
    private static final float SOUL_MAX_BACKUP_TIMER = 0.01f;
    private static final float SOUL_MAX_STUTTER_TIMER = 0.0f;
    private static final float SOUL_MAX_VFX_TIMER = 0.005f;

    @SpirePatch(
            clz = Soul.class,
            method = "update"
    )
    public static class FastSoulPatch {
        public static void Prefix(Soul __instance) {
            try {
                ReflectionHacks.setPrivate(
                        __instance,
                        Soul.class,
                        "backUpTimer",
                        SOUL_MAX_BACKUP_TIMER
                );

                ReflectionHacks.setPrivate(
                        __instance,
                        Soul.class,
                        "spawnStutterTimer",
                        SOUL_MAX_STUTTER_TIMER
                );

                ReflectionHacks.setPrivate(
                        __instance,
                        Soul.class,
                        "vfxTimer",
                        SOUL_MAX_VFX_TIMER
                );

                float currentSpeed = ReflectionHacks.getPrivate(
                        __instance,
                        Soul.class,
                        "currentSpeed"
                );

                if (currentSpeed < SOUL_MIN_SPEED) {
                    ReflectionHacks.setPrivate(
                            __instance,
                            Soul.class,
                            "currentSpeed",
                            SOUL_MIN_SPEED
                    );
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static final float CARD_LERP_STRENGTH = 0.85f;

    @SpirePatch(
            clz = AbstractCard.class,
            method = "update"
    )
    public static class FastCardLerpPatch {
        public static void Postfix(AbstractCard __instance) {
            if (__instance == null) return;

            __instance.current_x += (__instance.target_x - __instance.current_x) * CARD_LERP_STRENGTH;
            __instance.current_y += (__instance.target_y - __instance.current_y) * CARD_LERP_STRENGTH;

            __instance.drawScale += (__instance.targetDrawScale - __instance.drawScale) * CARD_LERP_STRENGTH;
            __instance.angle += (__instance.targetAngle - __instance.angle) * CARD_LERP_STRENGTH;
        }
    }




    @SpirePatch(
            clz = UseCardAction.class,
            method = "update"
    )
    public static class FastUseCardActionUpdatePatch {
        @SpirePostfixPatch
        public static void Postfix(UseCardAction __instance) {
            if (__instance.isDone) return;

            float duration = ReflectionHacks.getPrivate(
                    __instance,
                    com.megacrit.cardcrawl.actions.AbstractGameAction.class,
                    "duration"
            );

            // After the important "duration == 0.15F" block has run,
            // reduce the remaining animation time.
            if (duration < 0.15f && duration > 0.01f) {
                ReflectionHacks.setPrivate(
                        __instance,
                        com.megacrit.cardcrawl.actions.AbstractGameAction.class,
                        "duration",
                        0.001f
                );
            }
        }
    }

    @SpirePatch(
            clz = DamageAction.class,
            method = "update"
    )
    public static class FastDamageActionUpdatePatch {
        public static void Postfix(DamageAction __instance) {
            if (__instance.isDone) return;

            try {
                float duration = ReflectionHacks.getPrivate(
                        __instance,
                        AbstractGameAction.class,
                        "duration"
                );

                if (duration < 0.1f && duration > 0.01f) {
                    ReflectionHacks.setPrivate(
                            __instance,
                            AbstractGameAction.class,
                            "duration",
                            0.001f
                    );
                }
            } catch (Exception ignored) {
            }
        }
    }


    private static final float GAME_EFFECT_MAX_DURATION = 0.0002f;

    @SpirePatch(
            clz = AbstractGameEffect.class,
            method = "update"
    )
    public static class FastAbstractGameEffectPatch {
        public static void Prefix(AbstractGameEffect __instance) {
            if (__instance == null) return;

            if (__instance.duration > GAME_EFFECT_MAX_DURATION) {
                __instance.duration = GAME_EFFECT_MAX_DURATION;
            }

            if (__instance.startingDuration > GAME_EFFECT_MAX_DURATION) {
                __instance.startingDuration = GAME_EFFECT_MAX_DURATION;
            }
        }
    }
}