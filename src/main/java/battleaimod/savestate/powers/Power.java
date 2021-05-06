package battleaimod.savestate.powers;

import battleaimod.savestate.powers.powerstates.*;
import battleaimod.savestate.powers.powerstates.common.*;
import battleaimod.savestate.powers.powerstates.defect.*;
import battleaimod.savestate.powers.powerstates.ironclad.*;
import battleaimod.savestate.powers.powerstates.monsters.*;
import battleaimod.savestate.powers.powerstates.silent.*;
import battleaimod.savestate.powers.powerstates.watcher.*;
import com.megacrit.cardcrawl.powers.AbstractPower;

import java.util.function.Function;

public enum Power {
    ACCURACY("Accuracy", power -> new AccuracyPowerState(power)),
    ADAPTATION("Adaptation", power -> new RushdownPowerState(power)),
    AFTER_IMAGE("After Image", power -> new AfterImagePowerState(power)),
    AMPLIFY("Amplify", power -> new AmplifyPowerState(power)),
    ANGEL_FORM("AngelForm", power -> new LiveForeverPowerState(power)),
    ANGER("Anger", power -> new AngerPowerState(power)),
    ANGRY("Angry", power -> new AngryPowerState(power)),
    ARTIFACT("Artifact", power -> new ArtifactPowerState(power)),
    ATTACK_BURN("Attack Burn", power -> new AttackBurnPowerState(power)),
    BACK_ATTACK("BackAttack", power -> new BackAttackPowerState(power)),
    BARRICADE("Barricade", power -> new BarricadePowerState(power)),
    BATTLE_HYMN("BattleHymn", power -> new BattleHymnPowerState(power)),
    BEAT_OF_DEATH("BeatOfDeath", power -> new BeatOfDeathPowerState(power)),
    BERSERK("Berserk", power -> new BerserkPowerState(power)),
    BIAS("Bias", power -> new BiasPowerState(power)),
    BLOCK_RETURN_POWER("BlockReturnPower", power -> new BlockReturnPowerState(power)),
    BLUR("Blur", power -> new BlurPowerState(power)),
    BRUTALITY("Brutality", power -> new BrutalityPowerState(power)),
    BUFFER("Buffer", power -> new BufferPowerState(power)),
    BURST("Burst", power -> new BurstPowerState(power)),
    CANNOT_CHANGE_STANCE_POWER("CannotChangeStancePower", power -> new CannotChangeStancePowerState(power)),
    CHOKED("Choked", power -> new ChokePowerState(power)),
    CURL_UP("Curl Up", power -> new CurlUpPowerState(power)),
    COLLECT("Collect", power -> new CollectPowerState(power)),
    COMBUST("Combust", power -> new CombustPowerState(power), json -> new CombustPowerState(json)),
    COMPULSIVE("Compulsive", power -> new ReactivePowerState(power)),
    CONFUSION("Confusion", power -> new ConfusionPowerState(power)),
    CONSERVE("Conserve", power -> new ConservePowerState(power)),
    CONSTRICTED("Constricted", power -> new ConstrictedPowerState(power), json -> new ConstrictedPowerState(json)),
    CONTROLLED("Controlled", power -> new MentalFortressPowerState(power)),
    CORPSE_EXPLOSION_POWER("CorpseExplosionPower", power -> new CorpseExplosionPowerState(power)),
    CORRUPTION("Corruption", power -> new CorruptionPowerState(power)),
    CREATIVE_AI("Creative AI", power -> new CreativeAIPowerState(power)),
    CURIOSITY("Curiosity", power -> new CuriosityPowerState(power)),
    DARK_EMBRACE("Dark Embrace", power -> new DarkEmbracePowerState(power)),
    DEMON_FORM("Demon Form", power -> new DemonFormPowerState(power)),
    DEVA_FORM("DevaForm", power -> new DevaPowerState(power)),
    DEVOTION_POWER("DevotionPower", power -> new DevotionPowerState(power)),
    DEX_LOSS("DexLoss", power -> new LoseDexterityPowerState(power)),
    DEXTERITY("Dexterity", power -> new DexterityPowerState(power)),
    DOUBLE_DAMAGE("Double Damage", power -> new DoubleDamagePowerState(power)),
    DOUBLE_TAP("Double Tap", power -> new DoubleTapPowerState(power)),
    DRAW("Draw", power -> new DrawPowerState(power)),
    DRAW_CARD("Draw Card", power -> new DrawCardNextTurnPowerState(power)),
    DRAW_REDUCTION("Draw Reduction", power -> new DrawReductionPowerState(power), json -> new DrawReductionPowerState(json)),
    DUPLICATION_POWER("DuplicationPower", power -> new DuplicationPowerState(power)),
    ECHO_FORM("Echo Form", power -> new EchoPowerState(power), json -> new EchoPowerState(json)),
    ELECTRO("Electro", power -> new ElectroPowerState(power)),
    END_TURN_DEATH("EndTurnDeath", power -> new EndTurnDeathPowerState(power)),
    ENTANGLED("Entangled", power -> new EntanglePowerState(power)),
    ENERGIZED("Energized", power -> new EnergizedPowerState(power)),
    ENERGIZED_BLUE("EnergizedBlue", power -> new EnergizedBluePowerState(power)),
    ENVENOM("Envenom", power -> new EnvenomPowerState(power)),
    EQUILIBRIUM("Equilibrium", power -> new EquilibriumPowerState(power)),
    ESTABLISHMENT_POWER("EstablishmentPower", power -> new EstablishmentPowerState(power)),
    EVOLVE("Evolve", power -> new EvolvePowerState(power)),
    EXPLOSIVE("Explosive", power -> new ExplosivePowerState(power)),
    FADING("Fading", power -> new FadingPowerState(power)),
    FEEL_NO_PAIN("Feel No Pain", power -> new FeelNoPainPowerState(power)),
    FIRE_BREATHING("Fire Breathing", power -> new FireBreathingPowerState(power)),
    FLAME_BARRIER("Flame Barrier", power -> new FlameBarrierPowerState(power)),
    FLEX("Flex", power -> new LoseStrengthPowerState(power)),
    FLIGHT("Flight", power -> new FlightPowerState(power), json -> new FlightPowerState(json)),
    FRAIL("Frail", power -> new FrailPowerState(power)),
    FREE_ATTACK_POWER("FreeAttackPower", power -> new FreeAttackPowerState(power)),
    GENERIC_STRENGTH_UP_POWER("Generic Strength Up Power", power -> new GenericStrengthUpPowerState(power)),
    HELLO("Hello", power -> new HelloPowerState(power)),
    HEX("Hex", power -> new HexPowerState(power)),
    INFINITE_BLADES("Infinite Blades", power -> new InfiniteBladesPowerState(power)),
    INTANGIBLE("Intangible", power -> new IntangiblePowerState(power), json -> new IntangiblePowerState(json)),
    INTANGIBLE_PLAYER("IntangiblePlayer", power -> new IntangiblePlayerPowerState(power)),
    INVINCIBLE("Invincible", power -> new InvinciblePowerState(power)),
    JUGGERNAUT("Juggernaut", power -> new JuggernautPowerState(power)),
    LIFE_LINK("Life Link", power -> new RegrowPowerState(power)),
    LIKE_WATER_POWER("LikeWaterPower", power -> new LikeWaterPowerState(power)),
    LOCK_ON("Lockon", power -> new LockOnPowerState(power)),
    LOOP("Loop", power -> new LoopPowerState(power)),
    MAGNETISM("Magnetism", power -> new MagnetismPowerState(power)),
    MALLEABLE("Malleable", power -> new MalleablePowerState(power), json -> new MalleablePowerState(json)),
    MANTRA("Mantra", power -> new MantraPowerState(power)),
    MASTER_REALITY_POWER("MasterRealityPower", power -> new MasterRealityPowerState(power)),
    MAYHEM("Mayhem", power -> new MayhemPowerState(power)),
    METALLICIZE("Metallicize", power -> new MetallicizePowerState(power)),
    NEXT_TURN_BLOCK("Next Turn Block", power -> new NextTurnBlockPowerState(power)),
    MINION("Minion", power -> new MinionPowerState(power)),
    MODE_SHIFT("Mode Shift", power -> new ModeShiftPowerState(power)),
    NIRVANA("Nirvana", power -> new NirvanaPowerState(power)),
    NO_BLOCK_POWER("NoBlockPower", power -> new NoBlockPowerState(power)),
    NO_DRAW("No Draw", power -> new NoDrawPowerState(power)),
    NO_SKILLS("NoSkills", power -> new NoSkillsPowerState(power)),
    NOXIOUS_FLUMES("Noxious Fumes", power -> new NoxiousFumesPowerState(power)),
    OMEGA_POWER("OmegaPower", power -> new OmegaPowerState(power)),
    OMNISCIENCE_POWER("OmnisciencePower", power -> new OmnisciencePowerState(power)),
    PAINFUL_STABS("Painful Stabs", power -> new PainfulStabsPowerState(power)),
    PATH_TO_VICTORY_POWER("PathToVictoryPower", power -> new MarkPowerState(power)),
    PHANTASMAL("Phantasmal", power -> new PhantasmalPowerState(power)),
    POISON("Poison", power -> new PoisonPowerState(power)),
    RAGE("Rage", power -> new RagePowerState(power)),
    REGENERATION("Regeneration", power -> new RegenPowerState(power)),
    PEN_NIB("Pen Nib", power -> new PenNibPowerState(power)),
    PLATED_ARMOR("Plated Armor", power -> new PlatedArmorPowerState(power)),
    REBOUND("Rebound", power -> new ReboundPowerState(power)),
    REGENRATE("Regenerate", power -> new RegenerateMonsterPowerState(power)),
    REPAIR("Repair", power -> new RepairPowerState(power)),
    RETAIN_CARDS("Retain Cards", power -> new RetainCardPowerState(power)),
    RITUAL("Ritual", power -> new RitualPowerState(power), json -> new RitualPowerState(json)),
    RUPTURE("Rupture", power -> new RupturePowerState(power)),
    SADISTIC("Sadistic", power -> new SadisticPowerState(power)),
    SHACKLED("Shackled", power -> new GainStrengthPowerState(power)),
    SHIFTING("Shifting", power -> new ShiftingPowerState(power)),
    SKILL_BURN("Skill Burn", power -> new SkillBurnPowerState(power)),
    SLOW("Slow", power -> new SlowPowerState(power)),
    SPORE_CLOUD("Spore Cloud", power -> new SporeCloudPowerState(power)),
    SHARP_HIDE("Sharp Hide", power -> new SharpHidePowerState(power)),
    SPLIT("Split", power -> new SplitPowerState(power)),
    STASIS("Stasis", power -> new StasisPowerState(power), json -> new StasisPowerState(json)),
    STATIC_DISCHARGE("StaticDischarge", power -> new StaticDischargePowerState(power)),
    STORM("Storm", power -> new StormPowerState(power)),
    STRENGTH("Strength", power -> new StrengthPowerState(power)),
    STRIKE_UP("StrikeUp", power -> new StrikeUpPowerState(power)),
    STUDY("Study", power -> new StudyPowerState(power)),
    SURROUNDED("Surrounded", power -> new SurroundedPowerState(power)),
    THIEVERY("Thievery", power -> new ThieveryPowerState(power)),
    THOUSAND_CUTS("Thousand Cuts", power -> new ThousandCutsPowerState(power)),
    TIME_MAZE_POWER("TimeMazePower", power -> new TimeMazePowerState(power), json -> new TimeMazePowerState(json)),
    TIME_WARP("Time Warp", power -> new TimeWarpPowerState(power)),
    THORNS("Thorns", power -> new ThornsPowerState(power)),
    TOOLS_OF_THE_TRADE("Tools Of The Trade", power -> new ToolsOfTheTradePowerState(power)),
    UNAWAKENED("Unawakened", power -> new UnawakenedPowerState(power)),
    VIGOR("Vigor", power -> new VigorPowerState(power)),
    VULNERABLE("Vulnerable", power -> new VulnerablePowerState(power)),
    WAVE_OF_THE_HAND_POWER("WaveOfTheHandPower", power -> new WaveOfTheHandPowerState(power)),
    WEAKENED("Weakened", power -> new WeakenedPowerState(power), json -> new WeakenedPowerState(json)),
    WIRE_HEADING_POWER("WireheadingPower", power -> new ForesightPowerState(power)),
    WRATH_NEXT_TURN_POWER("WrathNextTurnPower", power -> new WrathNextTurnPowerState(power)),
    WRAITH_FORM_V2("Wraith Form v2", power -> new WraithFormPowerState(power)),
    ;

    public final String powerId;
    public final Function<AbstractPower, PowerState> factory;
    public final Function<String, PowerState> jsonFactory;

    Power(String id, Function<AbstractPower, PowerState> factory) {
        this.powerId = id;
        this.factory = factory;

        // parsing the ID and amount is enough for most powers
        this.jsonFactory = jsonString -> new PowerState(jsonString);
    }

    Power(String id, Function<AbstractPower, PowerState> factory, Function<String, PowerState> jsonFactory) {
        this.powerId = id;
        this.factory = factory;
        this.jsonFactory = jsonFactory;
    }
}
