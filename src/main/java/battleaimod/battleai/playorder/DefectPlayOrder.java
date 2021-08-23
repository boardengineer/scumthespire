package battleaimod.battleai.playorder;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.*;
import com.megacrit.cardcrawl.cards.colorless.*;
import com.megacrit.cardcrawl.cards.green.GrandFinale;
import com.megacrit.cardcrawl.cards.red.Corruption;

import java.util.HashMap;

public class DefectPlayOrder {
    public static HashMap<String, Integer> uglyThing;
    public static final HashMap<String, Integer> CARD_RANKS = makeRank();

    public static HashMap<String, Integer> makeRank() {
        uglyThing = new HashMap<>();

        add(new GrandFinale());
        add(new SadisticNature());
        add(new Amplify());
        add(new Storm());
        add(new Corruption());
        add(new Apotheosis());
        add(new Sunder());
        add(new Seek());

        // 0 costs
        add(new BeamCell());
        add(new Trip());
        add(new FTL());
        add(new Finesse());
        add(new Panache());
        add(new Claw());
        add(new Zap());
        add(new GoForTheEyes());
        add(new SteamBarrier());

        add(new MeteorStrike());

        add(new DoubleEnergy());
        add(new Scrape());

        add(new GeneticAlgorithm());

        add(new CoreSurge());

        add(new Defragment());
        add(new Aggregate());
        add(new AutoShields());
        add(new BallLightning());
        add(new BiasedCognition());
        add(new Blizzard());
        add(new BootSequence());
        add(new Buffer());
        add(new Capacitor());
        add(new Chaos());
        add(new Chill());
        add(new ColdSnap());
        add(new CompileDriver());
        add(new ConserveBattery());
        add(new Consume());
        add(new Coolheaded());
        add(new CreativeAI());
        add(new Darkness());
        add(new DoomAndGloom());
        add(new EchoForm());
        add(new Electrodynamics());
        add(new Fission());
        add(new Apparition());

        add(new Dualcast());
        add(new ForceField());
        add(new Fusion());
        add(new Glacier());
        add(new Heatsinks());
        add(new HelloWorld());
        add(new Hologram());
        add(new Hyperbeam());
        add(new Leap());
        add(new LockOn());
        add(new Loop());
        add(new MachineLearning());
        add(new Melter());
        add(new MultiCast());
        add(new Overclock());
        add(new Rainbow());
        add(new Rebound());
        add(new Recursion());
        add(new Recycle());
        add(new ReinforcedBody());
        add(new Reprogram());
        add(new RipAndTear());
        add(new SelfRepair());
        add(new Skim());
        add(new Stack());
        add(new StaticDischarge());
        add(new Streamline());
        add(new SweepingBeam());
        add(new Tempest());
        add(new ThunderStrike());
        add(new Turbo());
        add(new Equilibrium());
        add(new WhiteNoise());

        add(new Barrage());

        add(new AllForOne());

        add(new Reboot());
        add(new Strike_Blue());
        add(new Defend_Blue());
        return uglyThing;
    }

    private static void add(AbstractCard card) {
        uglyThing.put(card.cardID, uglyThing.size());
    }
}
