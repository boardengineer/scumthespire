package battleaimod.savestate;

import basemod.ReflectionHacks;
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

    private final boolean redSkullIsActive;

    private final boolean lanternFirstTurn;

    private final boolean centennialPuzzleUsedThisCombat;

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

        if(relic instanceof GamblingChip) {
            this.gamblingChipActivated = ReflectionHacks
                    .getPrivate(relic, GamblingChip.class, "activated");
        } else {
            this.gamblingChipActivated = false;
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
    }

    public AbstractRelic loadRelic() {
        AbstractRelic result;

        if (relicId.equals("Mummified Hand")) {
            result = new NoLoggerMummifiedHand();
        } else {
            result = RelicLibrary.getRelic(relicId).makeCopy();
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

        if(result instanceof GamblingChip) {
            ReflectionHacks
                    .setPrivate(result, GamblingChip.class, "activated", gamblingChipActivated);
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

        return relicStateJson.toString();
    }
}
