package battleaimod.savestate;

import basemod.ReflectionHacks;
import battleaimod.BattleAiMod;
import battleaimod.fastobjects.NoLoggerMummifiedHand;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.*;

public class RelicState {
    private final String relicId;
    private final int counter;
    private final boolean grayscale;

    private final boolean orichalcumTrigger;

    private final boolean necronomiconActivated;
    private final boolean gamblingChipActivated;

    private final boolean unceasingTopCanDraw;
    private final boolean unceasingTopDisabledUntilEndOfTurn;

    private final boolean redSkullIsActive;

    private final boolean lanternFirstTurn;

    private final boolean pocketwatchFirstTurn;

    private final boolean centennialPuzzleUsedThisCombat;

    private final boolean hoveringKiteTriggeredThisTurn;

    public RelicState(AbstractRelic relic) {
        this.relicId = relic.relicId;
        this.counter = relic.counter;

        this.grayscale = relic.grayscale;

        if (relic instanceof Orichalcum) {
            this.orichalcumTrigger = ((Orichalcum) relic).trigger;
        } else {
            orichalcumTrigger = false;
        }

        if (relic instanceof Necronomicon) {
            this.necronomiconActivated = ReflectionHacks
                    .getPrivate(relic, Necronomicon.class, "activated");
        } else {
            necronomiconActivated = false;
        }

        if (relic instanceof RedSkull) {
            this.redSkullIsActive = ReflectionHacks
                    .getPrivate(relic, RedSkull.class, "isActive");
        } else {
            this.redSkullIsActive = false;
        }

        if (relic instanceof Lantern) {
            this.lanternFirstTurn = ReflectionHacks
                    .getPrivate(relic, Lantern.class, "firstTurn");
        } else {
            this.lanternFirstTurn = false;
        }

        if (relic instanceof CentennialPuzzle) {
            this.centennialPuzzleUsedThisCombat = ReflectionHacks
                    .getPrivate(relic, CentennialPuzzle.class, "usedThisCombat");
        } else {
            this.centennialPuzzleUsedThisCombat = false;
        }

        if (relic instanceof GamblingChip) {
            this.gamblingChipActivated = ReflectionHacks
                    .getPrivate(relic, GamblingChip.class, "activated");
        } else {
            this.gamblingChipActivated = false;
        }

        if (relic instanceof UnceasingTop) {
            this.unceasingTopDisabledUntilEndOfTurn = ReflectionHacks
                    .getPrivate(relic, UnceasingTop.class, "disabledUntilEndOfTurn");
            this.unceasingTopCanDraw = ReflectionHacks
                    .getPrivate(relic, UnceasingTop.class, "canDraw");
        } else {
            this.unceasingTopDisabledUntilEndOfTurn = false;
            this.unceasingTopCanDraw = false;
        }

        if (relic instanceof Pocketwatch) {
            this.pocketwatchFirstTurn = ReflectionHacks
                    .getPrivate(relic, Pocketwatch.class, "firstTurn");
        } else {
            this.pocketwatchFirstTurn = false;
        }

        if (relic instanceof HoveringKite) {
            this.hoveringKiteTriggeredThisTurn = ReflectionHacks
                    .getPrivate(relic, HoveringKite.class, "triggeredThisTurn");
        } else {
            this.hoveringKiteTriggeredThisTurn = false;
        }
    }

    public RelicState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.relicId = parsed.get("relic_id").getAsString();
        this.counter = parsed.get("counter").getAsInt();
        this.grayscale = parsed.get("grayscale").getAsBoolean();

        this.orichalcumTrigger = parsed.get("orichalcum_trigger").getAsBoolean();

        this.necronomiconActivated = parsed.get("necronomicon_activated").getAsBoolean();

        this.redSkullIsActive = parsed.get("red_skull_is_active").getAsBoolean();

        this.lanternFirstTurn = parsed.get("lantern_first_turn").getAsBoolean();

        this.centennialPuzzleUsedThisCombat = parsed.get("centennial_puzzle_used_this_combat")
                                                    .getAsBoolean();
        this.gamblingChipActivated = parsed.get("gambling_chip_activated").getAsBoolean();
        this.unceasingTopDisabledUntilEndOfTurn = parsed
                .get("unceasing_top_disabled_until_end_of_turn").getAsBoolean();
        this.unceasingTopCanDraw = parsed.get("unceasing_top_can_draw").getAsBoolean();
        this.pocketwatchFirstTurn = parsed.get("pocketwatch_first_turn").getAsBoolean();
        this.hoveringKiteTriggeredThisTurn = parsed.get("hovering_kite_triggered_this_turn")
                                                   .getAsBoolean();
    }

    public AbstractRelic loadRelic() {
        AbstractRelic result;

        long makeRelicCopyStartTime = System.currentTimeMillis();

        if (relicId.equals("Mummified Hand")) {
            result = new NoLoggerMummifiedHand();
        } else {
            result = RelicLibrary.getRelic(relicId).makeCopy();
        }

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController.addRuntime("Load Time Relic Copy", System
                    .currentTimeMillis() - makeRelicCopyStartTime);
        }

        result.counter = counter;
        result.grayscale = grayscale;

        if (result instanceof Orichalcum) {
            ((Orichalcum) result).trigger = orichalcumTrigger;
        }

        if (result instanceof Necronomicon) {
            ReflectionHacks
                    .setPrivate(result, Necronomicon.class, "activated", necronomiconActivated);
        }

        if (result instanceof RedSkull) {
            ReflectionHacks
                    .setPrivate(result, RedSkull.class, "isActive", redSkullIsActive);
        }

        if (result instanceof Lantern) {
            ReflectionHacks
                    .setPrivate(result, Lantern.class, "firstTurn", lanternFirstTurn);
        }

        if (result instanceof CentennialPuzzle) {
            ReflectionHacks
                    .setPrivate(result, CentennialPuzzle.class, "usedThisCombat", centennialPuzzleUsedThisCombat);
        }

        if (result instanceof GamblingChip) {
            ReflectionHacks
                    .setPrivate(result, GamblingChip.class, "activated", gamblingChipActivated);
        }

        if (result instanceof UnceasingTop) {
            ReflectionHacks
                    .setPrivate(result, UnceasingTop.class, "canDraw", unceasingTopCanDraw);
            ReflectionHacks
                    .setPrivate(result, UnceasingTop.class, "disabledUntilEndOfTurn", unceasingTopDisabledUntilEndOfTurn);
        }

        if (result instanceof Pocketwatch) {
            ReflectionHacks
                    .setPrivate(result, Pocketwatch.class, "firstTurn", pocketwatchFirstTurn);
        }

        if (result instanceof HoveringKite) {
            ReflectionHacks
                    .setPrivate(result, HoveringKite.class, "triggeredThisTurn", hoveringKiteTriggeredThisTurn);
        }

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController.addRuntime("Load Time Load Relic", System
                    .currentTimeMillis() - makeRelicCopyStartTime);
        }

        return result;
    }

    public String encode() {
        JsonObject relicStateJson = new JsonObject();

        relicStateJson.addProperty("relic_id", relicId);
        relicStateJson.addProperty("counter", counter);
        relicStateJson.addProperty("grayscale", grayscale);

        relicStateJson.addProperty("orichalcum_trigger", orichalcumTrigger);
        relicStateJson.addProperty("necronomicon_activated", necronomiconActivated);

        relicStateJson.addProperty("red_skull_is_active", redSkullIsActive);

        relicStateJson.addProperty("lantern_first_turn", lanternFirstTurn);

        relicStateJson
                .addProperty("centennial_puzzle_used_this_combat", centennialPuzzleUsedThisCombat);

        relicStateJson.addProperty("gambling_chip_activated", gamblingChipActivated);

        relicStateJson.addProperty("unceasing_top_can_draw", unceasingTopCanDraw);
        relicStateJson
                .addProperty("unceasing_top_disabled_until_end_of_turn", unceasingTopDisabledUntilEndOfTurn);

        relicStateJson.addProperty("pocketwatch_first_turn", pocketwatchFirstTurn);
        relicStateJson.addProperty("hovering_kite_triggered_this_turn", hoveringKiteTriggeredThisTurn);

        return relicStateJson.toString();
    }
}
