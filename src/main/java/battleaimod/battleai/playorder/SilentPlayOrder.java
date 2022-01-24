package battleaimod.battleai.playorder;

import com.megacrit.cardcrawl.cards.blue.Claw;
import com.megacrit.cardcrawl.cards.blue.CreativeAI;
import com.megacrit.cardcrawl.cards.colorless.*;
import com.megacrit.cardcrawl.cards.curses.Pain;
import com.megacrit.cardcrawl.cards.green.*;
import com.megacrit.cardcrawl.cards.purple.LessonLearned;
import com.megacrit.cardcrawl.cards.purple.TalkToTheHand;
import com.megacrit.cardcrawl.cards.purple.WaveOfTheHand;
import com.megacrit.cardcrawl.cards.red.DarkEmbrace;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;

import java.util.HashMap;

public class SilentPlayOrder {
    public static final HashMap<String, Integer> CARD_RANKS = new HashMap<String, Integer>() {{
        int size = 0;

        put(new Pain().cardID, size++);
        put(new Panache().cardID, size++);
        put(new GrandFinale().cardID, size++);
        put(new MasterOfStrategy().cardID, size++);
        put(new SadisticNature().cardID, size++);
        put(new Apotheosis().cardID, size++);
        put(new CreativeAI().cardID, size++);
        put(new LessonLearned().cardID, size++);
        put(new WaveOfTheHand().cardID, size++);

        put(new Backflip().cardID, size++);
        put(new DarkEmbrace().cardID, size++);
        put(new Acrobatics().cardID, size++);
        put(new BulletTime().cardID, size++);
        put(new Transmutation().cardID, size++);
        put(new Claw().cardID, size++);

        put(new TalkToTheHand().cardID, size++);
        put(new Flechettes().cardID, size++);
        put(new Burst().cardID, size++);
        put(new Apparition().cardID, size++);
        put(new Footwork().cardID, size++);
        put(new Adrenaline().cardID, size++);
        put(new Malaise().cardID, size++);
        put(new ToolsOfTheTrade().cardID, size++);
        put(new Choke().cardID, size++);
        put(new AfterImage().cardID, size++);
        put(new Caltrops().cardID, size++);
        put(new InfiniteBlades().cardID, size++);
        put(new LegSweep().cardID, size++);
        put(new NoxiousFumes().cardID, size++);
        put(new CripplingPoison().cardID, size++);
        put(new Envenom().cardID, size++);
        put(new AThousandCuts().cardID, size++);
        put(new Nightmare().cardID, size++);
        put(new Alchemize().cardID, size++);
        put(new PhantasmalKiller().cardID, size++);
        put(new Outmaneuver().cardID, size++);
        put(new Accuracy().cardID, size++);
        put(new CalculatedGamble().cardID, size++);

        put(new BouncingFlask().cardID, size++);
        put(new CorpseExplosion().cardID, size++);
        put(new DeadlyPoison().cardID, size++);
        put(new PoisonedStab().cardID, size++);

        put(new Catalyst().cardID, size++);

        put(new Terror().cardID, size++);
        put(new WellLaidPlans().cardID, size++);

        put(new Dash().cardID, size++);
        put(new Skewer().cardID, size++);
        put(new RiddleWithHoles().cardID, size++);
        put(new Neutralize().cardID, size++);
        put(new QuickSlash().cardID, size++);
        put(new SuckerPunch().cardID, size++);
        put(new AllOutAttack().cardID, size++);
        put(new Backstab().cardID, size++);
        put(new HeelHook().cardID, size++);
        put(new Bane().cardID, size++);
        put(new FlyingKnee().cardID, size++);
        put(new DaggerThrow().cardID, size++);
        put(new DieDieDie().cardID, size++);
        put(new Slice().cardID, size++);
        put(new DaggerSpray().cardID, size++);
        put(new GlassKnife().cardID, size++);
        put(new MasterfulStab().cardID, size++);
        put(new Eviscerate().cardID, size++);
        put(new Predator().cardID, size++);
        put(new CloakAndDagger().cardID, size++);

        put(new SneakyStrike().cardID, size++);

        put(new Shiv().cardID, size++);

        put(new StormOfSteel().cardID, size++);
        put(new BladeDance().cardID, size++);
        put(new Strike_Green().cardID, size++);

        put(new Reflex().cardID, size++);
        put(new Setup().cardID, size++);
        put(new Tactician().cardID, size++);
        put(new WraithForm().cardID, size++);

        put(new Unload().cardID, size++);

        put(new Finisher().cardID, size++);

        put(new Doppelganger().cardID, size++);

        put(new Survivor().cardID, size++);
        put(new Prepared().cardID, size++);
        put(new PiercingWail().cardID, size++);
        put(new Distraction().cardID, size++);
        put(new EndlessAgony().cardID, size++);
        put(new DodgeAndRoll().cardID, size++);
        put(new Blur().cardID, size++);
        put(new EscapePlan().cardID, size++);
        put(new Deflect().cardID, size++);
        put(new Defend_Green().cardID, size++);

        put(new Expertise().cardID, size++);
        put(new Concentrate().cardID, size++);
    }};
}
