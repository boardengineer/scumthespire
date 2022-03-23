package autoplay.battleaimod.server.battleai.playorder;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.*;
import com.megacrit.cardcrawl.cards.colorless.*;
import com.megacrit.cardcrawl.cards.green.AfterImage;
import com.megacrit.cardcrawl.cards.green.Burst;
import com.megacrit.cardcrawl.cards.green.CalculatedGamble;
import com.megacrit.cardcrawl.cards.green.GrandFinale;
import com.megacrit.cardcrawl.cards.purple.TalkToTheHand;
import com.megacrit.cardcrawl.cards.red.Corruption;
import com.megacrit.cardcrawl.cards.red.Rage;

import java.util.Comparator;
import java.util.HashMap;

public class DefectPlayOrder {
    public static final HashMap<String, Integer> CARD_RANKS = new HashMap<String, Integer>() {{
        int size = 0;

        put(AfterImage.ID, size++);
        put(GrandFinale.ID, size++);
        put(SadisticNature.ID, size++);
        put(Amplify.ID, size++);
        put(Storm.ID, size++);
        put(Corruption.ID, size++);
        put(Apotheosis.ID, size++);
        put(Sunder.ID, size++);
        put(Seek.ID, size++);
        put(TalkToTheHand.ID, size++);

        // 0 costs
        put(Rage.ID, size++);
        put(CalculatedGamble.ID, size++);
        put(BeamCell.ID, size++);
        put(Trip.ID, size++);
        put(FTL.ID, size++);
        put(Finesse.ID, size++);
        put(Panache.ID, size++);
        put(Claw.ID, size++);
        put(Zap.ID, size++);
        put(GoForTheEyes.ID, size++);

        put(AllForOne.ID, size++);
        put(CalculatedGamble.ID, size++);
        put(SteamBarrier.ID, size++);

        put(MeteorStrike.ID, size++);

        put(DoubleEnergy.ID, size++);
        put(Scrape.ID, size++);

        put(GeneticAlgorithm.ID, size++);


        put(CoreSurge.ID, size++);

        put(Defragment.ID, size++);
        put(Aggregate.ID, size++);
        put(AutoShields.ID, size++);
        put(BallLightning.ID, size++);
        put(BiasedCognition.ID, size++);
        put(Blizzard.ID, size++);
        put(BootSequence.ID, size++);
        put(Buffer.ID, size++);
        put(Capacitor.ID, size++);
        put(Chaos.ID, size++);
        put(Chill.ID, size++);
        put(ColdSnap.ID, size++);
        put(CompileDriver.ID, size++);
        put(ConserveBattery.ID, size++);
        put(Consume.ID, size++);
        put(Coolheaded.ID, size++);
        put(CreativeAI.ID, size++);
        put(Darkness.ID, size++);
        put(DoomAndGloom.ID, size++);
        put(EchoForm.ID, size++);
        put(Electrodynamics.ID, size++);
        put(Fission.ID, size++);
        put(Apparition.ID, size++);

        put(Burst.ID, size++);
        put(Dualcast.ID, size++);
        put(ForceField.ID, size++);
        put(Fusion.ID, size++);
        put(Glacier.ID, size++);
        put(Heatsinks.ID, size++);
        put(HelloWorld.ID, size++);
        put(Hologram.ID, size++);
        put(Hyperbeam.ID, size++);
        put(Leap.ID, size++);
        put(LockOn.ID, size++);
        put(Loop.ID, size++);
        put(MachineLearning.ID, size++);
        put(Melter.ID, size++);
        put(MultiCast.ID, size++);
        put(Overclock.ID, size++);
        put(Rainbow.ID, size++);
        put(Rebound.ID, size++);
        put(Recursion.ID, size++);
        put(Recycle.ID, size++);
        put(ReinforcedBody.ID, size++);
        put(Reprogram.ID, size++);
        put(RipAndTear.ID, size++);
        put(SelfRepair.ID, size++);
        put(Skim.ID, size++);
        put(Stack.ID, size++);
        put(StaticDischarge.ID, size++);
        put(Streamline.ID, size++);
        put(SweepingBeam.ID, size++);
        put(Tempest.ID, size++);
        put(ThunderStrike.ID, size++);
        put(Turbo.ID, size++);
        put(Equilibrium.ID, size++);
        put(WhiteNoise.ID, size++);

        put(Barrage.ID, size++);

        put(Reboot.ID, size++);
        put(Strike_Blue.ID, size++);
        put(Defend_Blue.ID, size++);
    }};

    public static final Comparator<AbstractCard> COMPARATOR = (card1, card2) -> {
        if (CARD_RANKS.containsKey(card1.cardID) && CARD_RANKS
                .containsKey(card2.cardID)) {
            return CARD_RANKS.get(card1.cardID) - CARD_RANKS
                    .get(card2.cardID);
        }
        return 0;
    };
}
