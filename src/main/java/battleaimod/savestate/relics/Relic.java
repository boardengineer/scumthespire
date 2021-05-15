package battleaimod.savestate.relics;

import com.megacrit.cardcrawl.relics.*;

import java.util.function.Function;

public enum Relic {
    CENTENNIAL_PUZZLE(CentennialPuzzle.ID, relic -> new CentennialPuzzleState(relic), json -> new CentennialPuzzleState(json)),
    GAMBLING_CHIP(GamblingChip.ID, relic -> new GamblingChipState(relic), json -> new GamblingChipState(json)),
    HOVERING_KITE(HoveringKite.ID, relic -> new HoveringKiteState(relic), json -> new HoveringKiteState(json)),
    LANTERN(Lantern.ID, relic -> new LanternState(relic), json -> new LanternState(json)),
    NECRONOMICON(Necronomicon.ID, relic -> new NecronomiconState(relic), json -> new NecronomiconState(json)),
    ORICHALCUM(Orichalcum.ID, relic -> new OrichalcumState(relic), json -> new OrichalcumState(json)),
    POCKETWATCH(Pocketwatch.ID, relic -> new PocketwatchState(relic), json -> new PocketwatchState(json)),
    RED_SKULL(RedSkull.ID, relic -> new RedSkullState(relic), json -> new RedSkullState(json)),
    RUNIC_CAPACITOR(RunicCapacitor.ID, relic -> new RunicCapacitorState(relic), json -> new RunicCapacitorState(json)),
    UNCEASING_TOP(UnceasingTop.ID, relic -> new UnceasingTopState(relic), json -> new UnceasingTopState(json));

    public final String relicId;
    public final Function<AbstractRelic, RelicState> factory;
    public final Function<String, RelicState> jsonFactory;

    Relic() {
        this.relicId = "";

        factory = null;
        jsonFactory = null;
    }

    Relic(String relicId) {
        this.relicId = relicId;

        this.factory = null;
        this.jsonFactory = null;
    }

    Relic(String relicId, Function<AbstractRelic, RelicState> factory, Function<String, RelicState> jsonFactory) {
        this.relicId = relicId;
        this.factory = factory;
        this.jsonFactory = jsonFactory;
    }
}
