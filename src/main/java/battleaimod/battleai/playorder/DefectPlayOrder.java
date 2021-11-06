package battleaimod.battleai.playorder;

import com.megacrit.cardcrawl.cards.blue.*;
import com.megacrit.cardcrawl.cards.colorless.*;
import com.megacrit.cardcrawl.cards.green.AfterImage;
import com.megacrit.cardcrawl.cards.green.Burst;
import com.megacrit.cardcrawl.cards.green.CalculatedGamble;
import com.megacrit.cardcrawl.cards.green.GrandFinale;
import com.megacrit.cardcrawl.cards.purple.TalkToTheHand;
import com.megacrit.cardcrawl.cards.red.Corruption;
import com.megacrit.cardcrawl.cards.red.Rage;

import java.util.HashMap;

public class DefectPlayOrder {
    public static final HashMap<String, Integer> CARD_RANKS = new HashMap<String, Integer>() {{
        int size = 0;

        put(new AfterImage().cardID, size++);
        put(new GrandFinale().cardID, size++);
        put(new SadisticNature().cardID, size++);
        put(new Amplify().cardID, size++);
        put(new Storm().cardID, size++);
        put(new Corruption().cardID, size++);
        put(new Apotheosis().cardID, size++);
        put(new Sunder().cardID, size++);
        put(new Seek().cardID, size++);
        put(new TalkToTheHand().cardID, size++);

        // 0 costs
        put(new Rage().cardID, size++);
        put(new CalculatedGamble().cardID, size++);
        put(new BeamCell().cardID, size++);
        put(new Trip().cardID, size++);
        put(new FTL().cardID, size++);
        put(new Finesse().cardID, size++);
        put(new Panache().cardID, size++);
        put(new Claw().cardID, size++);
        put(new Zap().cardID, size++);
        put(new GoForTheEyes().cardID, size++);

        put(new AllForOne().cardID, size++);
        put(new CalculatedGamble().cardID, size++);
        put(new SteamBarrier().cardID, size++);

        put(new MeteorStrike().cardID, size++);

        put(new DoubleEnergy().cardID, size++);
        put(new Scrape().cardID, size++);

        put(new GeneticAlgorithm().cardID, size++);

        put(new CoreSurge().cardID, size++);

        put(new Defragment().cardID, size++);
        put(new Aggregate().cardID, size++);
        put(new AutoShields().cardID, size++);
        put(new BallLightning().cardID, size++);
        put(new BiasedCognition().cardID, size++);
        put(new Blizzard().cardID, size++);
        put(new BootSequence().cardID, size++);
        put(new Buffer().cardID, size++);
        put(new Capacitor().cardID, size++);
        put(new Chaos().cardID, size++);
        put(new Chill().cardID, size++);
        put(new ColdSnap().cardID, size++);
        put(new CompileDriver().cardID, size++);
        put(new ConserveBattery().cardID, size++);
        put(new Consume().cardID, size++);
        put(new Coolheaded().cardID, size++);
        put(new CreativeAI().cardID, size++);
        put(new Darkness().cardID, size++);
        put(new DoomAndGloom().cardID, size++);
        put(new EchoForm().cardID, size++);
        put(new Electrodynamics().cardID, size++);
        put(new Fission().cardID, size++);
        put(new Apparition().cardID, size++);

        put(new Burst().cardID, size++);
        put(new Dualcast().cardID, size++);
        put(new ForceField().cardID, size++);
        put(new Fusion().cardID, size++);
        put(new Glacier().cardID, size++);
        put(new Heatsinks().cardID, size++);
        put(new HelloWorld().cardID, size++);
        put(new Hologram().cardID, size++);
        put(new Hyperbeam().cardID, size++);
        put(new Leap().cardID, size++);
        put(new LockOn().cardID, size++);
        put(new Loop().cardID, size++);
        put(new MachineLearning().cardID, size++);
        put(new Melter().cardID, size++);
        put(new MultiCast().cardID, size++);
        put(new Overclock().cardID, size++);
        put(new Rainbow().cardID, size++);
        put(new Rebound().cardID, size++);
        put(new Recursion().cardID, size++);
        put(new Recycle().cardID, size++);
        put(new ReinforcedBody().cardID, size++);
        put(new Reprogram().cardID, size++);
        put(new RipAndTear().cardID, size++);
        put(new SelfRepair().cardID, size++);
        put(new Skim().cardID, size++);
        put(new Stack().cardID, size++);
        put(new StaticDischarge().cardID, size++);
        put(new Streamline().cardID, size++);
        put(new SweepingBeam().cardID, size++);
        put(new Tempest().cardID, size++);
        put(new ThunderStrike().cardID, size++);
        put(new Turbo().cardID, size++);
        put(new Equilibrium().cardID, size++);
        put(new WhiteNoise().cardID, size++);

        put(new Barrage().cardID, size++);

        put(new Reboot().cardID, size++);
        put(new Strike_Blue().cardID, size++);
        put(new Defend_Blue().cardID, size++);
    }};
}
