package battleaimod.patches;

import battleaimod.BattleAiMod;
import battleaimod.fastobjects.AnimationStateFast;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.BlockImpactLineEffect;
import com.megacrit.cardcrawl.vfx.combat.BlockedNumberEffect;
import com.megacrit.cardcrawl.vfx.combat.BlockedWordEffect;
import com.megacrit.cardcrawl.vfx.combat.HbBlockBrokenEffect;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class IroncladPatches {
    @SpirePatch(
            clz = Ironclad.class,
            paramtypez = {DamageInfo.class},
            method = "damage"
    )
    public static class SpyOnPlayerDamageffectPatch {
        static long startEffect = 0;

        public static SpireReturn Prefix(Ironclad _instance, DamageInfo info) {
            if (shouldGoFast()) {
                startEffect = System.currentTimeMillis();
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }

        public static SpireReturn Postfix(Ironclad _instance, DamageInfo info) {
            if (shouldGoFast()) {
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController
                            .addRuntime("Ironclad damage", System
                                    .currentTimeMillis() - startEffect);
                }
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = Ironclad.class,
            paramtypez = {String.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 79)
        public static SpireReturn Sentry(Ironclad _instance, String playerName) {
            if (shouldGoFast()) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {SpriteBatch.class},
            method = "render"
    )
    public static class DisableRenderIroncladPatch {
        public static SpireReturn Prefix(AbstractPlayer _instance, SpriteBatch sprites) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = BlockedWordEffect.class,
            paramtypez = {AbstractCreature.class, float.class, float.class, String.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class SpyOnBlockedWordEffectPatch {
        static long startEffect = 0;

        public static void Prefix(BlockedWordEffect _instance, AbstractCreature target, float x, float y, String msg) {
            if (shouldGoFast()) {
                startEffect = System.currentTimeMillis();
            }
        }

        public static void Postfix(BlockedWordEffect _instance, AbstractCreature target, float x, float y, String msg) {
            if (shouldGoFast()) {
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController
                            .addRuntime("Blocked Word Effect Constructor", System
                                    .currentTimeMillis() - startEffect);
                }
            }
        }
    }

    @SpirePatch(
            clz = HbBlockBrokenEffect.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class SpyOnHbBlockBrokenEffectPatch {
        static long startEffect = 0;

        public static void Prefix(HbBlockBrokenEffect _instance, float x, float y) {
            if (shouldGoFast()) {
                startEffect = System.currentTimeMillis();
            }
        }

        public static void Postfix(HbBlockBrokenEffect _instance, float x, float y) {
            if (shouldGoFast()) {
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController
                            .addRuntime("HbBlockBrokenEffect Constructor", System
                                    .currentTimeMillis() - startEffect);
                }
            }
        }
    }

    @SpirePatch(
            clz = BlockImpactLineEffect.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class SpyOnHBlockImpactLineEffectPatch {
        static long startEffect = 0;

        public static void Prefix(BlockImpactLineEffect _instance, float x, float y) {
            if (shouldGoFast()) {
                startEffect = System.currentTimeMillis();
            }
        }

        public static void Postfix(BlockImpactLineEffect _instance, float x, float y) {
            if (shouldGoFast()) {
                if (BattleAiMod.battleAiController != null) {
//                    BattleAiMod.battleAiController
//                            .addRuntime("BlockImpactLineEffect Constructor", System
//                                    .currentTimeMillis() - startEffect);
                }
            }
        }
    }

    @SpirePatch(
            clz = BlockedNumberEffect.class,
            paramtypez = {float.class, float.class, String.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class SpyOnHBlockedNumberEffectPatch {
        static long startEffect = 0;

        public static void Prefix(BlockedNumberEffect _instance, float x, float y, String message) {
            if (shouldGoFast()) {
                startEffect = System.currentTimeMillis();
            }
        }

        public static void Postfix(BlockedNumberEffect _instance, float x, float y, String message) {
            if (shouldGoFast()) {
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController
                            .addRuntime("BlockedNumberEffect Constructor", System
                                    .currentTimeMillis() - startEffect);
                }
            }
        }
    }

    @SpirePatch(
            clz = BorderFlashEffect.class,
            paramtypez = {Color.class, boolean.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class SpyOnBorderFlashEffectPatch {
        static long startEffect = 0;

        public static void Prefix(BorderFlashEffect _instance, Color color, boolean additive) {
            if (shouldGoFast()) {
                startEffect = System.currentTimeMillis();
            }
        }

        public static void Postfix(BorderFlashEffect _instance, Color color, boolean additive) {
            if (shouldGoFast()) {
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController
                            .addRuntime("BorderFlashEffect Constructor", System
                                    .currentTimeMillis() - startEffect);
                }
            }
        }
    }

    @SpirePatch(
            clz = AbstractCreature.class,
            paramtypez = {},
            method = "healthBarUpdatedEvent"
    )
    public static class SpyOnhealthBarUpdatedEventPatch {
        static long startEffect = 0;

        public static void Prefix(AbstractCreature _instance) {
            if (shouldGoFast()) {
                startEffect = System.currentTimeMillis();
            }
        }

        public static void Postfix(AbstractCreature _instance) {
            if (shouldGoFast()) {
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController
                            .addRuntime("healthBarUpdatedEvent", System
                                    .currentTimeMillis() - startEffect);
                }
            }
        }
    }

    @SpirePatch(
            clz = AbstractCreature.class,
            paramtypez = {},
            method = "brokeBlock"
    )
    public static class SpyOnbrokeBlockPatch {
        static long startEffect = 0;

        public static void Prefix(AbstractCreature _instance) {
            if (shouldGoFast()) {
                startEffect = System.currentTimeMillis();
            }
        }

        public static void Postfix(AbstractCreature _instance) {
            if (shouldGoFast()) {
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController
                            .addRuntime("brokeBlock", System
                                    .currentTimeMillis() - startEffect);
                }
            }
        }
    }

    @SpirePatch(
            clz = AbstractPlayer.class,
            paramtypez = {DamageInfo.class},
            method = "damage"
    )
    public static class SpyOnGenericPlayerDamageffectPatch {
        static long startEffect = 0;
        static long magicStartEffect = 0;

        /*public static SpireReturn Prefix(AbstractPlayer _instance, DamageInfo info) {
            if (shouldGoFast()) {
                startEffect = System.currentTimeMillis();
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }

        public static SpireReturn Postfix(AbstractPlayer _instance, DamageInfo info) {
            if (shouldGoFast()) {
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController
                            .addRuntime("Player damage", System
                                    .currentTimeMillis() - startEffect);
                }
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }*/

        @SpireInsertPatch(loc = 1740)
        public static SpireReturn startInsert(AbstractPlayer _instance, DamageInfo info) {
            if (shouldGoFast()) {
                magicStartEffect = System.currentTimeMillis();
                return SpireReturn.Continue();
            }

            return SpireReturn.Continue();
        }

        @SpireInsertPatch(loc = 1742)
        public static SpireReturn endInsert(AbstractPlayer _instance, DamageInfo info) {
            if (shouldGoFast()) {
                if (BattleAiMod.battleAiController != null) {
                    BattleAiMod.battleAiController
                            .addRuntime("Magic Player damage", System
                                    .currentTimeMillis() - magicStartEffect);
                    BattleAiMod.battleAiController
                            .addRuntime("Magic Player damage instance", 1);
                }
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }
    }
}
