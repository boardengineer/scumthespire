package battleaimod.battleai.playorder;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.*;
import com.megacrit.cardcrawl.cards.green.PhantasmalKiller;
import com.megacrit.cardcrawl.cards.purple.*;
import com.megacrit.cardcrawl.cards.red.*;

import java.util.HashMap;

public class IronCladPlayOrder {
    public static HashMap<String, Integer> uglyThing;
    public static final HashMap<String, Integer> CARD_RANKS = makeRank();

    public static HashMap<String, Integer> makeRank() {
        uglyThing = new HashMap<>();

        add(new Omniscience());
        add(new Headbutt());
        add(new MasterReality());
        add(new Worship());
        add(new Panacea());
        add(new Madness());
        add(new Shockwave());
        add(new SpotWeakness());

        add(new Transmutation());
        add(new Rage());
        add(new Offering());
        add(new Intimidate());
        add(new Flex());
        add(new DoubleTap());
        add(new DualWield());
        add(new Clash());

        add(new Bloodletting());
        add(new Berserk());
        add(new BattleTrance());
        add(new Whirlwind());

        add(new Armaments());
        add(new Feed());
        add(new Enlightenment());
        add(new HandOfGreed());

        // Powers first
        add(new Inflame());
        add(new Rupture());
        add(new Juggernaut());
        add(new FireBreathing());
        add(new FeelNoPain());
        add(new Evolve());
        add(new DemonForm());
        add(new Barricade());
        add(new Brutality());
        add(new Combust());
        add(new Corruption());
        add(new DarkEmbrace());
        add(new LimitBreak());
        add(new Metallicize());
        add(new PhantasmalKiller());

        // damage
        add(new ThunderClap());
        add(new Bludgeon());
        add(new Bash());
        add(new Uppercut());
        add(new SearingBlow());
        add(new Reaper());
        add(new Rampage());
        add(new TwinStrike());
        add(new PommelStrike());
        add(new HeavyBlade());
        add(new WildStrike());
        add(new PerfectedStrike());
        add(new Carnage());
        add(new InfernalBlade());
        add(new Immolate());
        add(new IronWave());
        add(new FiendFire());
        add(new Dropkick());
        add(new Hemokinesis());
        add(new Clothesline());
        add(new Cleave());
        add(new BloodForBlood());
        add(new Pummel());
        add(new RecklessCharge());
        add(new SeverSoul());
        add(new SwordBoomerang());

        add(new Anger());

        // Block last
        add(new SeeingRed());
        add(new ShrugItOff());
        add(new GhostlyArmor());
        add(new FlameBarrier());
        add(new Disarm());
        add(new PowerThrough());
        add(new Impervious());
        add(new BurningPact());
        add(new SecondWind());
        add(new TrueGrit());

        add(new Entrench());
        add(new BodySlam());

        add(new Exhume());

        add(new Havoc());
        add(new Warcry());

        add(new Strike_Red());
        add(new Defend_Red());
        add(new Sentinel());

        // These are the watcher cards, the combined play order will be here
        add(new WreathOfFlame());

        add(new Ragnarok());
        add(new TalkToTheHand());
        add(new FlurryOfBlows());
        add(new Halt());
        add(new Prostrate());
        add(new SpiritShield());
        add(new SignatureMove());



        // powers
        add(new BattleHymn());
        add(new Fasting());
        add(new LikeWater());
        add(new MentalFortress());
        add(new Rushdown());
        add(new Study());
        add(new DevaForm());
        add(new Devotion());
        add(new Establishment());

        add(new Alpha());
        add(new Blasphemy());

        add(new WaveOfTheHand());
        add(new BowlingBash());
        add(new Brilliance());
        add(new CarveReality());
        add(new Collect());
        add(new Conclude());
        add(new ConjureBlade());
        add(new Consecrate());
        add(new CrushJoints());
        add(new CutThroughFate());
        add(new DeusExMachina());
        add(new EmptyFist());
        add(new EmptyMind());
        add(new Eruption());
        add(new Evaluate());
        add(new FearNoEvil());
        add(new FollowUp());
        add(new ForeignInfluence());
        add(new Foresight());
        add(new Indignation());
        add(new InnerPeace());
        add(new Judgement());
        add(new JustLucky());
        add(new LessonLearned());
        add(new Meditate());
        add(new Nirvana());
        add(new Pray());
        add(new PressurePoints());
        add(new ReachHeaven());
        add(new SandsOfTime());
        add(new SashWhip());
        add(new Scrawl());
        add(new SimmeringFury());
        add(new Swivel());
        add(new Tantrum());
        add(new ThirdEye());
        add(new Tranquility());
        add(new Vault());
        add(new Wallop());
        add(new Weave());
        add(new WheelKick());
        add(new Wish());

        // blocks
        add(new DeceiveReality());
        add(new Vigilance());
        add(new EmptyBody());
        add(new Sanctity());


        // retain cards towards the end?
        add(new Crescendo());
        add(new FlyingSleeves());
        add(new WindmillStrike());


        add(new Strike_Purple());
        add(new Defend_Watcher());
        add(new Protect());
        add(new Perseverance());

        return uglyThing;
    }

    private static void add(AbstractCard card) {
        uglyThing.put(card.cardID, uglyThing.size());
    }
}
