package battleaimod.battleai.playorder;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.Claw;
import com.megacrit.cardcrawl.cards.blue.CreativeAI;
import com.megacrit.cardcrawl.cards.colorless.*;
import com.megacrit.cardcrawl.cards.curses.*;
import com.megacrit.cardcrawl.cards.green.*;
import com.megacrit.cardcrawl.cards.purple.LessonLearned;
import com.megacrit.cardcrawl.cards.purple.TalkToTheHand;
import com.megacrit.cardcrawl.cards.purple.WaveOfTheHand;
import com.megacrit.cardcrawl.cards.red.DarkEmbrace;
import com.megacrit.cardcrawl.cards.status.Burn;
import com.megacrit.cardcrawl.cards.status.Dazed;
import com.megacrit.cardcrawl.cards.status.Wound;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;

import java.util.Comparator;
import java.util.HashMap;

public class SilentPlayOrder {
    public static final HashMap<String, Integer> CARD_RANKS = new HashMap<String, Integer>() {{
        int size = 0;

        put(Pain.ID, size++);
        put(Panache.ID, size++);
        put(GrandFinale.ID, size++);
        put(MasterOfStrategy.ID, size++);
        put(SadisticNature.ID, size++);
        put(Apotheosis.ID, size++);
        put(CreativeAI.ID, size++);
        put(LessonLearned.ID, size++);
        put(WaveOfTheHand.ID, size++);

        put(Wound.ID, size++);
        put(Burn.ID, size++);
        put(Dazed.ID, size++);
        put(Pain.ID, size++);
        put(Doubt.ID, size++);
        put(Normality.ID, size++);
        put(Shame.ID, size++);
        put(AscendersBane.ID, size++);
        put(CurseOfTheBell.ID, size++);

        put(CorpseExplosion.ID, size++);

        put(Backflip.ID, size++);
        put(DarkEmbrace.ID, size++);
        put(Acrobatics.ID, size++);
        put(BulletTime.ID, size++);
        put(Transmutation.ID, size++);
        put(Claw.ID, size++);

        put(TalkToTheHand.ID, size++);
        put(Flechettes.ID, size++);
        put(Burst.ID, size++);
        put(Apparition.ID, size++);
        put(Footwork.ID, size++);
        put(Adrenaline.ID, size++);
        put(Malaise.ID, size++);
        put(ToolsOfTheTrade.ID, size++);
        put(Choke.ID, size++);
        put(AfterImage.ID, size++);
        put(Caltrops.ID, size++);
        put(InfiniteBlades.ID, size++);
        put(LegSweep.ID, size++);
        put(NoxiousFumes.ID, size++);
        put(CripplingPoison.ID, size++);
        put(Envenom.ID, size++);
        put(AThousandCuts.ID, size++);
        put(Nightmare.ID, size++);
        put(Alchemize.ID, size++);
        put(PhantasmalKiller.ID, size++);
        put(Outmaneuver.ID, size++);
        put(Accuracy.ID, size++);
        put(CalculatedGamble.ID, size++);

        put(BouncingFlask.ID, size++);
        put(DeadlyPoison.ID, size++);
        put(PoisonedStab.ID, size++);

        put(Catalyst.ID, size++);

        put(Terror.ID, size++);
        put(WellLaidPlans.ID, size++);

        put(Dash.ID, size++);
        put(Skewer.ID, size++);
        put(RiddleWithHoles.ID, size++);
        put(Neutralize.ID, size++);
        put(QuickSlash.ID, size++);
        put(SuckerPunch.ID, size++);
        put(AllOutAttack.ID, size++);
        put(Backstab.ID, size++);
        put(HeelHook.ID, size++);
        put(Bane.ID, size++);
        put(FlyingKnee.ID, size++);
        put(DaggerThrow.ID, size++);
        put(DieDieDie.ID, size++);
        put(Slice.ID, size++);
        put(DaggerSpray.ID, size++);
        put(GlassKnife.ID, size++);
        put(MasterfulStab.ID, size++);
        put(Eviscerate.ID, size++);
        put(Predator.ID, size++);
        put(CloakAndDagger.ID, size++);

        put(SneakyStrike.ID, size++);

        put(Shiv.ID, size++);

        put(StormOfSteel.ID, size++);
        put(BladeDance.ID, size++);
        put(Strike_Green.ID, size++);

        put(Reflex.ID, size++);
        put(Setup.ID, size++);
        put(Tactician.ID, size++);
        put(WraithForm.ID, size++);

        put(Unload.ID, size++);

        put(Finisher.ID, size++);

        put(Doppelganger.ID, size++);

        put(Survivor.ID, size++);
        put(Prepared.ID, size++);
        put(PiercingWail.ID, size++);
        put(Distraction.ID, size++);
        put(EndlessAgony.ID, size++);
        put(DodgeAndRoll.ID, size++);
        put(Blur.ID, size++);
        put(EscapePlan.ID, size++);
        put(Deflect.ID, size++);
        put(Defend_Green.ID, size++);

        put(Expertise.ID, size++);
        put(Concentrate.ID, size++);
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
