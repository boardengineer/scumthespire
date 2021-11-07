package battleaimod.battleai.playorder;

import com.megacrit.cardcrawl.cards.blue.WhiteNoise;
import com.megacrit.cardcrawl.cards.colorless.*;
import com.megacrit.cardcrawl.cards.green.PhantasmalKiller;
import com.megacrit.cardcrawl.cards.purple.*;
import com.megacrit.cardcrawl.cards.red.*;

import java.util.HashMap;

public class IronCladPlayOrder {
    public static final HashMap<String, Integer> CARD_RANKS = new HashMap<String, Integer>() {{
        int size = 0;

        put(new Omniscience().cardID, size++);
        put(new Headbutt().cardID, size++);
        put(new SadisticNature().cardID, size++);
        put(new MasterReality().cardID, size++);
        put(new Worship().cardID, size++);
        put(new Panacea().cardID, size++);
        put(new Madness().cardID, size++);
        put(new Shockwave().cardID, size++);
        put(new SpotWeakness().cardID, size++);

        put(new Transmutation().cardID, size++);
        put(new Rage().cardID, size++);
        put(new Offering().cardID, size++);
        put(new Intimidate().cardID, size++);
        put(new Flex().cardID, size++);
        put(new DoubleTap().cardID, size++);
        put(new DualWield().cardID, size++);
        put(new Clash().cardID, size++);

        put(new Bloodletting().cardID, size++);
        put(new Berserk().cardID, size++);
        put(new BattleTrance().cardID, size++);
        put(new Whirlwind().cardID, size++);

        put(new Armaments().cardID, size++);
        put(new Feed().cardID, size++);
        put(new Enlightenment().cardID, size++);
        put(new HandOfGreed().cardID, size++);

        // Powers first
        put(new Inflame().cardID, size++);
        put(new Rupture().cardID, size++);
        put(new Juggernaut().cardID, size++);
        put(new FireBreathing().cardID, size++);
        put(new FeelNoPain().cardID, size++);
        put(new Evolve().cardID, size++);
        put(new DemonForm().cardID, size++);
        put(new Barricade().cardID, size++);
        put(new Brutality().cardID, size++);
        put(new Combust().cardID, size++);
        put(new Corruption().cardID, size++);
        put(new DarkEmbrace().cardID, size++);
        put(new LimitBreak().cardID, size++);
        put(new Metallicize().cardID, size++);
        put(new PhantasmalKiller().cardID, size++);

        // damage
        put(new ThunderClap().cardID, size++);
        put(new Bludgeon().cardID, size++);
        put(new Bash().cardID, size++);
        put(new Uppercut().cardID, size++);
        put(new SearingBlow().cardID, size++);
        put(new Reaper().cardID, size++);
        put(new Rampage().cardID, size++);
        put(new TwinStrike().cardID, size++);
        put(new PommelStrike().cardID, size++);
        put(new HeavyBlade().cardID, size++);
        put(new WildStrike().cardID, size++);
        put(new PerfectedStrike().cardID, size++);
        put(new Carnage().cardID, size++);
        put(new InfernalBlade().cardID, size++);
        put(new Immolate().cardID, size++);
        put(new IronWave().cardID, size++);
        put(new FiendFire().cardID, size++);
        put(new Dropkick().cardID, size++);
        put(new Hemokinesis().cardID, size++);
        put(new Clothesline().cardID, size++);
        put(new Cleave().cardID, size++);
        put(new BloodForBlood().cardID, size++);
        put(new Pummel().cardID, size++);
        put(new RecklessCharge().cardID, size++);
        put(new SeverSoul().cardID, size++);
        put(new SwordBoomerang().cardID, size++);

        put(new Anger().cardID, size++);

        // Block last
        put(new SeeingRed().cardID, size++);
        put(new ShrugItOff().cardID, size++);
        put(new GhostlyArmor().cardID, size++);
        put(new FlameBarrier().cardID, size++);
        put(new Disarm().cardID, size++);
        put(new PowerThrough().cardID, size++);
        put(new Impervious().cardID, size++);
        put(new BurningPact().cardID, size++);
        put(new SecondWind().cardID, size++);
        put(new TrueGrit().cardID, size++);

        put(new Entrench().cardID, size++);
        put(new BodySlam().cardID, size++);

        put(new Exhume().cardID, size++);

        put(new Havoc().cardID, size++);
        put(new Warcry().cardID, size++);

        put(new Strike_Red().cardID, size++);
        put(new Defend_Red().cardID, size++);
        put(new Sentinel().cardID, size++);

        // These are the watcher cards, the combined play order will be here
        put(new WreathOfFlame().cardID, size++);

        put(new Ragnarok().cardID, size++);
        put(new TalkToTheHand().cardID, size++);
        put(new FlurryOfBlows().cardID, size++);
        put(new Halt().cardID, size++);
        put(new Prostrate().cardID, size++);
        put(new SpiritShield().cardID, size++);
        put(new SignatureMove().cardID, size++);

        // powers
        put(new BattleHymn().cardID, size++);
        put(new Fasting().cardID, size++);
        put(new LikeWater().cardID, size++);
        put(new MentalFortress().cardID, size++);
        put(new Rushdown().cardID, size++);
        put(new Study().cardID, size++);
        put(new DevaForm().cardID, size++);
        put(new Devotion().cardID, size++);
        put(new Establishment().cardID, size++);
        put(new WhiteNoise().cardID, size++);

        put(new Alpha().cardID, size++);
        put(new WaveOfTheHand().cardID, size++);
        put(new BowlingBash().cardID, size++);
        put(new Brilliance().cardID, size++);
        put(new CarveReality().cardID, size++);
        put(new Collect().cardID, size++);
        put(new Conclude().cardID, size++);
        put(new ConjureBlade().cardID, size++);
        put(new Consecrate().cardID, size++);
        put(new CrushJoints().cardID, size++);
        put(new CutThroughFate().cardID, size++);
        put(new DeusExMachina().cardID, size++);
        put(new EmptyFist().cardID, size++);
        put(new EmptyMind().cardID, size++);
        put(new Eruption().cardID, size++);
        put(new Evaluate().cardID, size++);
        put(new FearNoEvil().cardID, size++);
        put(new FollowUp().cardID, size++);
        put(new ForeignInfluence().cardID, size++);
        put(new Foresight().cardID, size++);
        put(new Indignation().cardID, size++);
        put(new InnerPeace().cardID, size++);
        put(new Judgement().cardID, size++);
        put(new JustLucky().cardID, size++);
        put(new LessonLearned().cardID, size++);
        put(new Meditate().cardID, size++);
        put(new Nirvana().cardID, size++);
        put(new Pray().cardID, size++);
        put(new PressurePoints().cardID, size++);
        put(new ReachHeaven().cardID, size++);
        put(new SandsOfTime().cardID, size++);
        put(new SashWhip().cardID, size++);
        put(new Scrawl().cardID, size++);
        put(new SimmeringFury().cardID, size++);
        put(new Swivel().cardID, size++);
        put(new Tantrum().cardID, size++);
        put(new ThirdEye().cardID, size++);
        put(new Tranquility().cardID, size++);
        put(new Vault().cardID, size++);
        put(new Wallop().cardID, size++);
        put(new Weave().cardID, size++);
        put(new WheelKick().cardID, size++);
        put(new Wish().cardID, size++);
        put(new Blasphemy().cardID, size++);

        // blocks
        put(new DeceiveReality().cardID, size++);
        put(new Vigilance().cardID, size++);
        put(new EmptyBody().cardID, size++);
        put(new Sanctity().cardID, size++);


        // retain cards towards the end?
        put(new Crescendo().cardID, size++);
        put(new FlyingSleeves().cardID, size++);
        put(new WindmillStrike().cardID, size++);


        put(new Strike_Purple().cardID, size++);
        put(new Defend_Watcher().cardID, size++);
        put(new Protect().cardID, size++);
        put(new Perseverance().cardID, size++);
    }};
}
