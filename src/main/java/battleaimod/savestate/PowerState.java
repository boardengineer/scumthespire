package battleaimod.savestate;

import basemod.ReflectionHacks;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.*;

public class PowerState {
    public final String powerId;
    public final int amount;

    private final int hpLoss;

    private final boolean ritualSkipFirst;

    private final CardState stasisCard;

    private final int malleableBasePower;

    private final int flightStoredAmount;

    public final int constrictedSourceIndex;

    public PowerState(AbstractPower power) {
        this.powerId = power.ID;
        this.amount = power.amount;

        if (power instanceof CombustPower) {
            this.hpLoss = ReflectionHacks
                    .getPrivate(power, CombustPower.class, "hpLoss");
        } else {
            this.hpLoss = 0;
        }

        if (power instanceof RitualPower) {
            ritualSkipFirst = ReflectionHacks
                    .getPrivate(power, RitualPower.class, "skipFirst");
        } else {
            ritualSkipFirst = false;
        }

        if (power instanceof StasisPower) {
            AbstractCard card = ReflectionHacks
                    .getPrivate(power, StasisPower.class, "card");
            stasisCard = new CardState(card);
        } else {
            stasisCard = null;
        }

        if (power instanceof MalleablePower) {
            malleableBasePower = ReflectionHacks
                    .getPrivate(power, MalleablePower.class, "basePower");
        } else {
            malleableBasePower = 0;
        }

        if (power instanceof FlightPower) {
            flightStoredAmount = ReflectionHacks
                    .getPrivate(power, FlightPower.class, "storedAmount");
        } else {
            flightStoredAmount = 0;
        }

        if (power instanceof ConstrictedPower) {
            AbstractCreature source = ReflectionHacks.getPrivate(power, ConstrictedPower.class, "source");

            int foundIndex = 0;
            for(int i = 0; i < AbstractDungeon.getMonsters().monsters.size(); i++) {
                if(source == AbstractDungeon.getMonsters().monsters.get(i)) {
                    foundIndex = i;
                    break;
                }
            }

            constrictedSourceIndex = foundIndex;
        } else {
            constrictedSourceIndex = 0;
        }
    }

    public PowerState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.powerId = parsed.get("power_id").getAsString();
        this.amount = parsed.get("amount").getAsInt();

        this.ritualSkipFirst = parsed.get("ritual_skip_first").getAsBoolean();

        JsonElement cardEle = parsed.get("stasis_card");
        if (cardEle.isJsonNull()) {
            stasisCard = null;
        } else {
            this.stasisCard = new CardState(cardEle.getAsString());
        }

        this.malleableBasePower = parsed.get("malleable_base_power").getAsInt();

        this.flightStoredAmount = parsed.get("flight_stored_amount").getAsInt();

        this.constrictedSourceIndex = parsed.get("constricted_source_index").getAsInt();

        // TODO
        this.hpLoss = 0;
    }

    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        AbstractPower result = null;
        if (powerId.equals("Strength")) {
            result = new StrengthPower(targetAndSource, amount);
        } else if (powerId.equals("Vulnerable")) {
            result = new VulnerablePower(targetAndSource, amount, false);
        } else if (powerId.equals("Ritual")) {
            result = new RitualPower(targetAndSource, amount, false);
            ReflectionHacks
                    .setPrivate(result, RitualPower.class, "skipFirst", ritualSkipFirst);
        } else if (powerId.equals("Weakened")) {
            result = new WeakPower(targetAndSource, amount, false);
        } else if (powerId.equals("Frail")) {
            result = new FrailPower(targetAndSource, amount, false);
        } else if (powerId.equals("Anger")) {
            result = new AngerPower(targetAndSource, amount);
        } else if (powerId.equals("Spore Cloud")) {
            result = new SporeCloudPower(targetAndSource, amount);
        } else if (powerId.equals("Thievery")) {
            result = new ThieveryPower(targetAndSource, amount);
        } else if (powerId.equals("Metallicize")) {
            result = new MetallicizePower(targetAndSource, amount);
        } else if (powerId.equals("Dexterity")) {
            result = new DexterityPower(targetAndSource, amount);
        } else if (powerId.equals("Curl Up")) {
            result = new CurlUpPower(targetAndSource, amount);
        } else if (powerId.equals("Flex")) {
            result = new LoseStrengthPower(targetAndSource, amount);
        } else if (powerId.equals("Artifact")) {
            result = new ArtifactPower(targetAndSource, amount);
        } else if (powerId.equals("Double Tap")) {
            result = new DoubleTapPower(targetAndSource, amount);
        } else if (powerId.equals("Split")) {
            result = new SplitPower(targetAndSource);
        } else if (powerId.equals("Combust")) {
            result = new CombustPower(targetAndSource, hpLoss, amount);
        } else if (powerId.equals("Evolve")) {
            result = new EvolvePower(targetAndSource, amount);
        } else if (powerId.equals("Mode Shift")) {
            result = new ModeShiftPower(targetAndSource, amount);
        } else if (powerId.equals("Angry")) {
            result = new AngryPower(targetAndSource, amount);
        } else if (powerId.equals("Sharp Hide")) {
            result = new SharpHidePower(targetAndSource, amount);
        } else if (powerId.equals("Entangled")) {
            result = new EntanglePower(targetAndSource);
        } else if (powerId.equals("Pen Nib")) {
            result = new PenNibPower(targetAndSource, amount);
        } else if (powerId.equals("Flame Barrier")) {
            result = new FlameBarrierPower(targetAndSource, amount);
        } else if (powerId.equals("Thorns")) {
            result = new ThornsPower(targetAndSource, amount);
        } else if (powerId.equals("IntangiblePlayer")) {
            result = new IntangiblePlayerPower(targetAndSource, amount);
        } else if (powerId.equals("Hex")) {
            result = new HexPower(targetAndSource, amount);
        } else if (powerId.equals("Plated Armor")) {
            result = new PlatedArmorPower(targetAndSource, amount);
        } else if (powerId.equals("Barricade")) {
            result = new BarricadePower(targetAndSource);
        } else if (powerId.equals("Minion")) {
            result = new MinionPower(targetAndSource);
        } else if (powerId.equals("Demon Form")) {
            result = new DemonFormPower(targetAndSource, amount);
        } else if (powerId.equals("Berserk")) {
            result = new BerserkPower(targetAndSource, amount);
        } else if (powerId.equals("Flight")) {
            result = new FlightPower(targetAndSource, amount);
            ReflectionHacks
                    .setPrivate(result, FlightPower.class, "storedAmount", flightStoredAmount);
        } else if (powerId.equals("Malleable")) {
            result = new MalleablePower(targetAndSource, amount);
        } else if (powerId.equals("Next Turn Block")) {
            result = new NextTurnBlockPower(targetAndSource, amount);
        } else if (powerId.equals("Painful Stabs")) {
            result = new PainfulStabsPower(targetAndSource);
        } else if (powerId.equals("Fire Breathing")) {
            result = new FireBreathingPower(targetAndSource, amount);
        } else if (powerId.equals("Corruption")) {
            result = new CorruptionPower(targetAndSource);
        } else if (powerId.equals("Buffer")) {
            result = new BufferPower(targetAndSource, amount);
        } else if (powerId.equals("Rupture")) {
            result = new RupturePower(targetAndSource, amount);
        } else if (powerId.equals("Confusion")) {
            result = new ConfusionPower(targetAndSource);
        } else if (powerId.equals("Regeneration")) {
            result = new RegenPower(targetAndSource, amount);
        } else if (powerId.equals("Generic Strength Up Power")) {
            result = new GenericStrengthUpPower(targetAndSource, "", amount);
        } else if (powerId.equals("Life Link")) {
            result = new RegrowPower(targetAndSource);
        } else if (powerId.equals("Stasis")) {
            result = new StasisPower(targetAndSource, stasisCard.loadCard());
        } else if (powerId.equals("Dark Embrace")) {
            result = new DarkEmbracePower(targetAndSource, amount);
        } else if (powerId.equals("Feel No Pain")) {
            result = new FeelNoPainPower(targetAndSource, amount);
        } else if (powerId.equals("Juggernaut")) {
            result = new JuggernautPower(targetAndSource, amount);
        } else if (powerId.equals("DuplicationPower")) {
            result = new DuplicationPower(targetAndSource, amount);
        } else if (powerId.equals("Rage")) {
            result = new RagePower(targetAndSource, amount);
        } else if (powerId.equals("DexLoss")) {
            result = new LoseDexterityPower(targetAndSource, amount);
        } else if (powerId.equals("Sadistic")) {
            result = new SadisticPower(targetAndSource, amount);
        } else if (powerId.equals("Brutality")) {
            result = new BrutalityPower(targetAndSource, amount);
        } else if (powerId.equals("Explosive")) {
            result = new ExplosivePower(targetAndSource, amount);
        } else if (powerId.equals("Fading")) {
            result = new FadingPower(targetAndSource, amount);
        } else if (powerId.equals("Shifting")) {
            result = new ShiftingPower(targetAndSource);
        } else if (powerId.equals("Shackled")) {
            result = new GainStrengthPower(targetAndSource, amount);
        } else if (powerId.equals("Intangible")) {
            result = new IntangiblePower(targetAndSource, amount);
        } else if (powerId.equals("Slow")) {
            result = new SlowPower(targetAndSource, amount);
        } else if (powerId.equals("Regenerate")) {
            result = new RegenerateMonsterPower((AbstractMonster) targetAndSource, amount);
        } else if (powerId.equals("Curiosity")) {
            result = new CuriosityPower(targetAndSource, amount);
        } else if (powerId.equals("Unawakened")) {
            result = new UnawakenedPower(targetAndSource);
        } else if (powerId.equals("Compulsive")) {
            result = new ReactivePower(targetAndSource);
        } else if (powerId.equals("Constricted")) {
            result = new ConstrictedPower(targetAndSource, null, amount);
        } else {
            System.err.println("missing type for power id: " + powerId);
        }

        return result;
    }

    public String encode() {
        JsonObject powerStateJson = new JsonObject();

        powerStateJson.addProperty("power_id", powerId);
        powerStateJson.addProperty("amount", amount);

        powerStateJson.addProperty("ritual_skip_first", ritualSkipFirst);
        powerStateJson.addProperty("stasis_card", stasisCard == null ? null : stasisCard.encode());
        powerStateJson.addProperty("malleable_base_power", malleableBasePower);
        powerStateJson.addProperty("flight_stored_amount", flightStoredAmount);
        powerStateJson.addProperty("constricted_source_index", constrictedSourceIndex);

        return powerStateJson.toString();
    }

}
