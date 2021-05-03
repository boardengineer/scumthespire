package battleaimod.savestate;

import basemod.ReflectionHacks;
import battleaimod.BattleAiMod;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.powers.watcher.*;

public class PowerState {
    public final String powerId;
    public final int amount;

    private final int hpLoss;

    private final boolean ritualSkipFirst;
    private final boolean intangibleJustApplied;

    private final boolean drawReductionJustApplied;
    private final boolean weakJustApplied;

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
            if (stasisCard == null) {
                throw new IllegalStateException("Bad stasis card");
            }
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
            AbstractCreature source = ReflectionHacks
                    .getPrivate(power, ConstrictedPower.class, "source");

            int foundIndex = 0;
            for (int i = 0; i < AbstractDungeon.getMonsters().monsters.size(); i++) {
                if (source == AbstractDungeon.getMonsters().monsters.get(i)) {
                    foundIndex = i;
                    break;
                }
            }

            constrictedSourceIndex = foundIndex;
        } else {
            constrictedSourceIndex = 0;
        }

        if (power instanceof IntangiblePower) {
            this.intangibleJustApplied = ReflectionHacks
                    .getPrivate(power, IntangiblePower.class, "justApplied");
        } else {
            this.intangibleJustApplied = false;
        }

        if (power instanceof DrawReductionPower) {
            this.drawReductionJustApplied = ReflectionHacks
                    .getPrivate(power, DrawReductionPower.class, "justApplied");
        } else {
            this.drawReductionJustApplied = false;
        }

        if (power instanceof WeakPower) {
            this.weakJustApplied = ReflectionHacks
                    .getPrivate(power, WeakPower.class, "justApplied");
        } else {
            this.weakJustApplied = false;
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

        this.intangibleJustApplied = parsed.get("intangible_just_applied").getAsBoolean();
        this.drawReductionJustApplied = parsed.get("draw_reduction_just_applied").getAsBoolean();

        // TODO
        this.hpLoss = 0;
        this.weakJustApplied = false;
    }

    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        long loadStartTime = System.currentTimeMillis();

        AbstractPower result = null;
        if (powerId.equals("Strength")) {
            result = new StrengthPower(targetAndSource, amount);
        } else if (powerId.equals("Vulnerable")) {
            result = new VulnerablePower(targetAndSource, amount, false);
        } else if (powerId.equals("Ritual")) {
            result = new RitualPower(targetAndSource, amount, targetAndSource == AbstractDungeon.player);
            ReflectionHacks
                    .setPrivate(result, RitualPower.class, "skipFirst", ritualSkipFirst);
        } else if (powerId.equals("Weakened")) {
            result = new WeakPower(targetAndSource, amount, weakJustApplied);
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
            ReflectionHacks
                    .setPrivate(result, MalleablePower.class, "basePower", malleableBasePower);
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
            AbstractCard resultCard = stasisCard.loadCard();

            if (resultCard == null) {
                throw new IllegalStateException("Card Returned Was Null");
            }

            result = new StasisPower(targetAndSource, resultCard);
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
            ReflectionHacks
                    .setPrivate(result, IntangiblePower.class, "justApplied", intangibleJustApplied);
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
        } else if (powerId.equals("No Draw")) {
            result = new NoDrawPower(targetAndSource);
            result.amount = amount;
        } else if (powerId.equals("Mayhem")) {
            result = new MayhemPower(targetAndSource, amount);
        } else if (powerId.equals("Time Warp")) {
            result = new TimeWarpPower(targetAndSource);
            result.amount = amount;
        } else if (powerId.equals("Vigor")) {
            result = new VigorPower(targetAndSource, amount);
        } else if (powerId.equals("Draw Reduction")) {
            result = new DrawReductionPower(targetAndSource, amount);
            ReflectionHacks
                    .setPrivate(result, DrawReductionPower.class, "justApplied", drawReductionJustApplied);
        } else if (powerId.equals("Accuracy"))  // Begin section
        {
            result = new AccuracyPower(targetAndSource, amount);
        } else if (powerId.equals("After Image")) {
            result = new AfterImagePower(targetAndSource, amount);
        } else if (powerId.equals("Amplify")) {
            result = new AmplifyPower(targetAndSource, amount);
        } else if (powerId.equals("Attack Burn")) {
            result = new AttackBurnPower(targetAndSource, amount);
        } else if (powerId.equals("Bias")) {
            result = new BiasPower(targetAndSource, amount);
        } else if (powerId.equals("Blur")) {
            result = new BlurPower(targetAndSource, amount);
        } else if (powerId.equals("Burst")) {
            result = new BurstPower(targetAndSource, amount);
        } else if (powerId.equals("Choked")) {
            result = new ChokePower(targetAndSource, amount);
        } else if (powerId.equals("Conserve")) {
            result = new ConservePower(targetAndSource, amount);
        } else if (powerId.equals("Creative AI")) {
            result = new CreativeAIPower(targetAndSource, amount);
        //} else if (powerId.equals("Double Damage")) {
        //    result = new DoubleDamagePower(targetAndSource, amount, TODO);
        } else if (powerId.equals("Draw")) {
            result = new DrawPower(targetAndSource, amount);
        //} else if (powerId.equals("Echo Form")) {
        //    result = new EchoPower(targetAndSource, amount);    // TODO
        } else if (powerId.equals("Electro")) {
            result = new ElectroPower(targetAndSource);
        } else if (powerId.equals("Energized")) {
            result = new EnergizedPower(targetAndSource, amount);
        } else if (powerId.equals("EnergizedBlue")) {
            result = new EnergizedBluePower(targetAndSource, amount);
        } else if (powerId.equals("Envenom")) {
            result = new EnvenomPower(targetAndSource, amount);
        } else if (powerId.equals("Focus")) {
            result = new FocusPower(targetAndSource, amount);
        //} else if (powerId.equals("GrowthPower")) {
        //    result = new GrowthPower(targetAndSource, amount);    // TODO
        } else if (powerId.equals("Heatsink")) {
            result = new HeatsinkPower(targetAndSource, amount);
        } else if (powerId.equals("Hello")) {
            result = new HelloPower(targetAndSource, amount);
        } else if (powerId.equals("Infinite Blades")) {
            result = new InfiniteBladesPower(targetAndSource, amount);
        //} else if (powerId.equals("Invincible")) {
        //    result = new InvinciblePower(targetAndSource, amount);  // TODO
        } else if (powerId.equals("BeatOfDeath")) {
            result = new BeatOfDeathPower(targetAndSource, amount);
        } else if (powerId.equals("Lockon")) {
            result = new LockOnPower(targetAndSource, amount);
        } else if (powerId.equals("Loop")) {
            result = new LoopPower(targetAndSource, amount);
        } else if (powerId.equals("Study")) {
            result = new StudyPower(targetAndSource, amount);
        } else if (powerId.equals("Magnetism")) {
            result = new MagnetismPower(targetAndSource, amount);
        //} else if (powerId.equals("Night Terror")) {
        //    result = new NightmarePower(targetAndSource, amount, card);   // TODO
        } else if (powerId.equals("Noxious Fumes")) {
            result = new NoxiousFumesPower(targetAndSource, amount);
        //} else if (powerId.equals("Panache")) {
        //    result = new PanachePower(targetAndSource, amount);   // TODO
        } else if (powerId.equals("Phantasmal")) {
            result = new PhantasmalPower(targetAndSource, amount);
        //} else if (powerId.equals("Poison")) {
        //    result = new PoisonPower();   // TODO!
        } else if (powerId.equals("Rebound")) {
            result = new ReboundPower(targetAndSource);    // TODO
        } else if (powerId.equals("Repair")) {
            result = new RepairPower(targetAndSource, amount);
        } else if (powerId.equals("Retain Cards")) {
            result = new RetainCardPower(targetAndSource, amount);
        //} else if (powerId.equals("Skill Burn")) {
        //    result = new SkillBurnPower(targetAndSource, amount);    // TODO
        } else if (powerId.equals("StaticDischarge")) {
            result = new StaticDischargePower(targetAndSource, amount);
        } else if (powerId.equals("Storm")) {
            result = new StormPower(targetAndSource, amount);
        //} else if (powerId.equals("TheBomb")) {
        //    result = new TheBombPower(targetAndSource, turns, amount);    // TODO
        } else if (powerId.equals("Thousand Cuts")) {
            result = new ThousandCutsPower(targetAndSource, amount);
        } else if (powerId.equals("Tools Of The Trade")) {
            result = new ToolsOfTheTradePower(targetAndSource, amount);
        } else if (powerId.equals("Wraith Form v2")) {
            result = new WraithFormPower(targetAndSource, amount);
        } else if (powerId.equals("Equilibrium")) {
            result = new EquilibriumPower(targetAndSource, amount);
        //} else if (powerId.equals("TimeMazePower")) {
        //    result = new TimeMazePower(targetAndSource, amount);    // TODO
        } else if (powerId.equals("NoBlockPower")) {
            result = new NoBlockPower(targetAndSource, amount, false);  // No monsters do this: only Panic Button.
        } else if (powerId.equals("CorpseExplosionPower")) {
            result = new CorpseExplosionPower(targetAndSource);
        } else if (powerId.equals("Surrounded")) {
            result = new SurroundedPower(targetAndSource);
        } else if (powerId.equals("Nirvana")) {
            result = new NirvanaPower(targetAndSource, amount);
        } else if (powerId.equals("BackAttack")) {
            result = new BackAttackPower(targetAndSource);
        } else if (powerId.equals("Adaptation")) {
            result = new RushdownPower(targetAndSource, amount);
        } else if (powerId.equals("Controlled")) {
            result = new MentalFortressPower(targetAndSource, amount);
        } else if (powerId.equals("Collect")) {
            result = new CollectPower(targetAndSource, amount);
        } else if (powerId.equals("EndTurnDeath")) {
            result = new EndTurnDeathPower(targetAndSource);
        } else if (powerId.equals("Mantra")) {
            result = new MantraPower(targetAndSource, amount);
        } else if (powerId.equals("AngelForm")) {
            result = new LiveForeverPower(targetAndSource, amount);
        } else if (powerId.equals("WireheadingPower")) {
            result = new ForesightPower(targetAndSource, amount);
        } else if (powerId.equals("EstablishmentPower")) {
            result = new EstablishmentPower(targetAndSource, amount);
        } else if (powerId.equals("DevotionPower")) {
            result = new DevotionPower(targetAndSource, amount);
        } else if (powerId.equals("BlockReturnPower")) {
            result = new BlockReturnPower(targetAndSource, amount);
        } else if (powerId.equals("BattleHymn")) {
            result = new BattleHymnPower(targetAndSource, amount);
        } else if (powerId.equals("FreeAttackPower")) {
            result = new FreeAttackPower(targetAndSource, amount);
        } else if (powerId.equals("NoSkills")) {
            result = new NoSkillsPower(targetAndSource);
        } else if (powerId.equals("MasterRealityPower")) {
            result = new MasterRealityPower(targetAndSource);
        } else if (powerId.equals("StrikeUp")) {
            result = new StrikeUpPower(targetAndSource, amount);
        } else if (powerId.equals("CannotChangeStancePower")) {
            result = new CannotChangeStancePower(targetAndSource);
        } else if (powerId.equals("OmegaPower")) {
            result = new OmegaPower(targetAndSource, amount);
        } else if (powerId.equals("WrathNextTurnPower")) {
            result = new WrathNextTurnPower(targetAndSource);
        //} else if (powerId.equals("DevaForm")) {
        //    result = new DevaPower(targetAndSource);    // TODO
        } else if (powerId.equals("OmnisciencePower")) {
            result = new OmnisciencePower(targetAndSource, amount);
        } else if (powerId.equals("WaveOfTheHandPower")) {
            result = new WaveOfTheHandPower(targetAndSource, amount);
        } else if (powerId.equals("EnergyDownPower")) {
            result = new EnergyDownPower(targetAndSource, amount);
        } else if (powerId.equals("PathToVictoryPower")) {
            result = new MarkPower(targetAndSource, amount);
        } else if (powerId.equals("LikeWaterPower")) {
            result = new LikeWaterPower(targetAndSource, amount);   // End section
        } else {
            System.err.println("missing type for power id: " + powerId);
        }

        if (BattleAiMod.battleAiController != null) {
            BattleAiMod.battleAiController.addRuntime("Load Time loading power", System
                    .currentTimeMillis() - loadStartTime);
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
        powerStateJson.addProperty("intangible_just_applied", intangibleJustApplied);
        powerStateJson.addProperty("draw_reduction_just_applied", drawReductionJustApplied);

        return powerStateJson.toString();
    }
}
