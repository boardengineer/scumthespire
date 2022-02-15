package battleaimod.battleai.playorder;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.Hologram;
import com.megacrit.cardcrawl.cards.blue.Seek;
import com.megacrit.cardcrawl.cards.blue.WhiteNoise;
import com.megacrit.cardcrawl.cards.colorless.*;
import com.megacrit.cardcrawl.cards.green.PhantasmalKiller;
import com.megacrit.cardcrawl.cards.purple.*;
import com.megacrit.cardcrawl.cards.red.*;

import java.util.Comparator;
import java.util.HashMap;

public class IronCladPlayOrder {
    public static final HashMap<String, Integer> CARD_RANKS = new HashMap<String, Integer>() {{
        int size = 0;

        put(Seek.ID, size++);
        put(Hologram.ID, size++);

        put(Apotheosis.ID, size++);
        put(Discovery.ID, size++);

        put(Omniscience.ID, size++);
        put(Headbutt.ID, size++);
        put(SadisticNature.ID, size++);
        put(MasterReality.ID, size++);
        put(Worship.ID, size++);
        put(Panacea.ID, size++);
        put(Madness.ID, size++);
        put(Shockwave.ID, size++);
        put(SpotWeakness.ID, size++);

        put(Transmutation.ID, size++);
        put(Rage.ID, size++);
        put(Offering.ID, size++);
        put(Intimidate.ID, size++);
        put(Flex.ID, size++);
        put(DoubleTap.ID, size++);
        put(DualWield.ID, size++);
        put(Clash.ID, size++);

        put(Bloodletting.ID, size++);
        put(Berserk.ID, size++);
        put(BattleTrance.ID, size++);
        put(Whirlwind.ID, size++);

        put(Armaments.ID, size++);
        put(Feed.ID, size++);
        put(Enlightenment.ID, size++);
        put(HandOfGreed.ID, size++);

        // Powers first
        put(Inflame.ID, size++);
        put(Rupture.ID, size++);
        put(Juggernaut.ID, size++);
        put(FireBreathing.ID, size++);
        put(FeelNoPain.ID, size++);
        put(Evolve.ID, size++);
        put(DemonForm.ID, size++);
        put(Barricade.ID, size++);
        put(Brutality.ID, size++);
        put(Combust.ID, size++);
        put(Corruption.ID, size++);
        put(DarkEmbrace.ID, size++);
        put(LimitBreak.ID, size++);
        put(Metallicize.ID, size++);
        put(PhantasmalKiller.ID, size++);

        // damage
        put(ThunderClap.ID, size++);
        put(Bludgeon.ID, size++);
        put(Bash.ID, size++);
        put(Uppercut.ID, size++);
        put(SearingBlow.ID, size++);
        put(Reaper.ID, size++);
        put(Rampage.ID, size++);
        put(TwinStrike.ID, size++);
        put(PommelStrike.ID, size++);
        put(HeavyBlade.ID, size++);
        put(WildStrike.ID, size++);
        put(PerfectedStrike.ID, size++);
        put(Carnage.ID, size++);
        put(InfernalBlade.ID, size++);
        put(Immolate.ID, size++);
        put(IronWave.ID, size++);
        put(FiendFire.ID, size++);
        put(Dropkick.ID, size++);
        put(Hemokinesis.ID, size++);
        put(Clothesline.ID, size++);
        put(Cleave.ID, size++);
        put(BloodForBlood.ID, size++);
        put(Pummel.ID, size++);
        put(RecklessCharge.ID, size++);
        put(SeverSoul.ID, size++);
        put(SwordBoomerang.ID, size++);

        put(Anger.ID, size++);

        // Block last
        put(SeeingRed.ID, size++);
        put(ShrugItOff.ID, size++);
        put(GhostlyArmor.ID, size++);
        put(FlameBarrier.ID, size++);
        put(Disarm.ID, size++);
        put(PowerThrough.ID, size++);
        put(Impervious.ID, size++);
        put(BurningPact.ID, size++);
        put(SecondWind.ID, size++);
        put(TrueGrit.ID, size++);

        put(Entrench.ID, size++);
        put(BodySlam.ID, size++);

        put(Exhume.ID, size++);

        put(Havoc.ID, size++);
        put(Warcry.ID, size++);

        put(Strike_Red.ID, size++);
        put(Defend_Red.ID, size++);
        put(Sentinel.ID, size++);

        // These are the watcher cards, the combined play order will be here
        put(WreathOfFlame.ID, size++);
        put(Weave.ID, size++);

        put(Ragnarok.ID, size++);
        put(TalkToTheHand.ID, size++);
        put(FlurryOfBlows.ID, size++);
        put(Halt.ID, size++);
        put(Prostrate.ID, size++);
        put(SpiritShield.ID, size++);
        put(SignatureMove.ID, size++);

        // powers
        put(BattleHymn.ID, size++);
        put(Fasting.ID, size++);
        put(LikeWater.ID, size++);
        put(MentalFortress.ID, size++);
        put(Rushdown.ID, size++);
        put(Study.ID, size++);
        put(DevaForm.ID, size++);
        put(Devotion.ID, size++);
        put(Establishment.ID, size++);
        put(WhiteNoise.ID, size++);

        put(Alpha.ID, size++);
        put(WaveOfTheHand.ID, size++);
        put(BowlingBash.ID, size++);
        put(Brilliance.ID, size++);
        put(CarveReality.ID, size++);
        put(Collect.ID, size++);
        put(Conclude.ID, size++);
        put(ConjureBlade.ID, size++);
        put(Consecrate.ID, size++);
        put(CrushJoints.ID, size++);
        put(CutThroughFate.ID, size++);
        put(DeusExMachina.ID, size++);
        put(EmptyFist.ID, size++);
        put(EmptyMind.ID, size++);
        put(Eruption.ID, size++);
        put(Evaluate.ID, size++);
        put(FearNoEvil.ID, size++);
        put(FollowUp.ID, size++);
        put(ForeignInfluence.ID, size++);
        put(Foresight.ID, size++);
        put(Indignation.ID, size++);
        put(InnerPeace.ID, size++);
        put(Judgement.ID, size++);
        put(JustLucky.ID, size++);
        put(LessonLearned.ID, size++);
        put(Meditate.ID, size++);
        put(Nirvana.ID, size++);
        put(Pray.ID, size++);
        put(PressurePoints.ID, size++);
        put(ReachHeaven.ID, size++);
        put(SandsOfTime.ID, size++);
        put(SashWhip.ID, size++);
        put(Scrawl.ID, size++);
        put(SimmeringFury.ID, size++);
        put(Swivel.ID, size++);
        put(Tantrum.ID, size++);
        put(ThirdEye.ID, size++);
        put(Tranquility.ID, size++);
        put(Vault.ID, size++);
        put(Wallop.ID, size++);
        put(WheelKick.ID, size++);
        put(Wish.ID, size++);
        put(Blasphemy.ID, size++);

        // blocks
        put(DeceiveReality.ID, size++);
        put(Vigilance.ID, size++);
        put(EmptyBody.ID, size++);
        put(Sanctity.ID, size++);


        // retain cards towards the end?
        put(Crescendo.ID, size++);
        put(FlyingSleeves.ID, size++);
        put(WindmillStrike.ID, size++);


        put(Strike_Purple.ID, size++);
        put(Defend_Watcher.ID, size++);
        put(Protect.ID, size++);
        put(Perseverance.ID, size++);
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
