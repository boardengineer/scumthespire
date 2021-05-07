package battleaimod.savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.potions.*;

import java.util.HashMap;
import java.util.HashSet;

public class PotionState {
    public final String potionId;
    private final int slot;

    public PotionState(AbstractPotion potion) {
        this.potionId = potion.ID;
        this.slot = potion.slot;
    }

    public PotionState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.potionId = parsed.get("id").getAsString();
        this.slot = parsed.get("slot").getAsInt();
    }

    public String encode() {
        JsonObject potionJson = new JsonObject();

        potionJson.addProperty("id", potionId);
        potionJson.addProperty("slot", slot);

        return potionJson.toString();
    }

    public AbstractPotion loadPotion() {
        AbstractPotion result;
        if (potionId.equals("Potion Slot")) {
            result = new PotionSlot(slot);
        } else {
            result = PotionHelper.getPotion(potionId).makeCopy();
        }

        result.slot = slot;

        return result;
    }

    public static final HashMap<String, Integer> POTION_VALUES = new HashMap<String, Integer>() {{
        put(Ambrosia.POTION_ID, 0);
        put(AncientPotion.POTION_ID, 20);
        put(AttackPotion.POTION_ID, 15);
        put(BlessingOfTheForge.POTION_ID, 20);
        put(BlockPotion.POTION_ID, 60);
        put(BloodPotion.POTION_ID, 120);
        put(BottledMiracle.POTION_ID, 0);
        put(ColorlessPotion.POTION_ID, 15);
        put(CultistPotion.POTION_ID, 30);
        put(CunningPotion.POTION_ID, 12);
        put(DexterityPotion.POTION_ID, 12);
        put(DistilledChaosPotion.POTION_ID, 40);
        put(DuplicationPotion.POTION_ID, 50);
        put(Elixir.POTION_ID, 20);
        put(EnergyPotion.POTION_ID, 80);
        put(EntropicBrew.POTION_ID, 240);
        put(EssenceOfDarkness.POTION_ID, 0);
        put(EssenceOfSteel.POTION_ID, 60);
        put(ExplosivePotion.POTION_ID, 40);
        put(FairyPotion.POTION_ID, 100);
        put(FearPotion.POTION_ID, 20);
        put(FirePotion.POTION_ID, 40);
        put(FocusPotion.POTION_ID, 0);

        // Just play fruit juice
        put(FruitJuice.POTION_ID, 0);
        put(GamblersBrew.POTION_ID, 500);
        put(GhostInAJar.POTION_ID, 120);
        put(HeartOfIron.POTION_ID, 150);
        put(LiquidBronze.POTION_ID, 50);
        put(LiquidMemories.POTION_ID, 30);
        put(PoisonPotion.POTION_ID, 20);
        put(PotionOfCapacity.POTION_ID, 0);
        put(SmokeBomb.POTION_ID, 0);
        put(PowerPotion.POTION_ID, 50);
        put(RegenPotion.POTION_ID, 100);
        put(SneckoOil.POTION_ID, 75);
        put(SpeedPotion.POTION_ID, 75);
        put(StancePotion.POTION_ID, 0);
        put(SteroidPotion.POTION_ID, 40);
        put(HeartOfIron.POTION_ID, 150);
        put(StrengthPotion.POTION_ID, 40);
        put(SwiftPotion.POTION_ID, 40);
        put(WeakenPotion.POTION_ID, 50);
    }};

    public static HashSet<String> UNPLAYABLE_POTIONS = new HashSet<String>() {{
        add(AttackPotion.POTION_ID);
        add(ColorlessPotion.POTION_ID);
        add(GamblersBrew.POTION_ID);
        add(LiquidMemories.POTION_ID);
        add(PowerPotion.POTION_ID);
        add(SkillPotion.POTION_ID);
        add(SmokeBomb.POTION_ID);
        add(Elixir.POTION_ID);

        // TODO: but why?
        add(DistilledChaosPotion.POTION_ID);
//        add(FirePotion.POTION_ID);
    }};
}
