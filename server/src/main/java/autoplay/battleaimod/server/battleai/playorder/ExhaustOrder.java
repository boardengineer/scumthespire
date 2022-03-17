package autoplay.battleaimod.server.battleai.playorder;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.cards.colorless.Transmutation;
import com.megacrit.cardcrawl.cards.curses.*;
import com.megacrit.cardcrawl.cards.red.*;
import com.megacrit.cardcrawl.cards.status.Burn;
import com.megacrit.cardcrawl.cards.status.Slimed;
import com.megacrit.cardcrawl.cards.status.Wound;

import java.util.Comparator;
import java.util.HashMap;

public class ExhaustOrder {
    public static final HashMap<String, Integer> CARD_RANKS = new HashMap<String, Integer>() {{
        int size = 0;

        put(Sentinel.ID, size++);

        // non-exhausting statuses and curses
        put(Normality.ID, size++);
        put(Burn.ID, size++);
        put(Pain.ID, size++);
        put(Regret.ID, size++);
        put(Shame.ID, size++);
        put(Writhe.ID, size++);
        put(Doubt.ID, size++);

        put(Wound.ID, size++);
        put(Slimed.ID, size++);
        put(CurseOfTheBell.ID, size++);
        put(Injury.ID, size++);
        put(Necronomicurse.ID, size++);

        // Strikes and Defends
        put(Defend_Red.ID, size++);
        put(Strike_Red.ID, size++);

        put(Madness.ID, size++);
        put(Shockwave.ID, size++);
        put(SpotWeakness.ID, size++);
        put(Transmutation.ID, size++);
        put(Whirlwind.ID, size++);
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

        put(Armaments.ID, size++);
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
        put(ThunderClap.ID, size++);
        put(Bludgeon.ID, size++);
        put(Bash.ID,size++);
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
        put(Headbutt.ID, size++);
        put(Feed.ID, size++);
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
