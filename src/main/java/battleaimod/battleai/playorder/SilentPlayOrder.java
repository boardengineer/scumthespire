package battleaimod.battleai.playorder;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Panache;
import com.megacrit.cardcrawl.cards.green.*;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;

import java.util.HashMap;

public class SilentPlayOrder {
    public static HashMap<String, Integer> uglyThing;
    public static final HashMap<String, Integer> CARD_RANKS = makeRank();

    public static HashMap<String, Integer> makeRank() {
        uglyThing = new HashMap<>();

        add(new Panache());
        add(new SneakyStrike());
        add(new GrandFinale());

        add(new Flechettes());
        add(new Burst());
        add(new Footwork());
        add(new Adrenaline());
        add(new Malaise());
        add(new ToolsOfTheTrade());
        add(new Choke());
        add(new AfterImage());
        add(new Caltrops());
        add(new InfiniteBlades());
        add(new LegSweep());
        add(new NoxiousFumes());
        add(new CripplingPoison());
        add(new Envenom());
        add(new AThousandCuts());
        add(new Nightmare());
        add(new Alchemize());
        add(new PhantasmalKiller());
        add(new Outmaneuver());
        add(new Accuracy());
        add(new CalculatedGamble());

        add(new BouncingFlask());
        add(new CorpseExplosion());
        add(new DeadlyPoison());
        add(new Catalyst());

        add(new Terror());
        add(new WellLaidPlans());

        add(new Backflip());
        add(new Acrobatics());
        add(new BulletTime());

        add(new Skewer());
        add(new RiddleWithHoles());
        add(new Neutralize());
        add(new QuickSlash());
        add(new SuckerPunch());
        add(new AllOutAttack());
        add(new Backstab());
        add(new HeelHook());
        add(new Bane());
        add(new FlyingKnee());
        add(new DaggerThrow());
        add(new DieDieDie());
        add(new Slice());
        add(new DaggerSpray());
        add(new GlassKnife());
        add(new PoisonedStab());
        add(new MasterfulStab());
        add(new Eviscerate());
        add(new Predator());
        add(new Unload());
        add(new CloakAndDagger());

        add(new Shiv());

        add(new StormOfSteel());
        add(new BladeDance());
        add(new Strike_Green());

        add(new Reflex());
        add(new Setup());
        add(new Tactician());
        add(new WraithForm());


        add(new Finisher());

        add(new Doppelganger());

        add(new Survivor());
        add(new Prepared());
        add(new PiercingWail());
        add(new Distraction());
        add(new EndlessAgony());
        add(new DodgeAndRoll());
        add(new Blur());
        add(new EscapePlan());
        add(new Dash());
        add(new Deflect());
        add(new Defend_Green());

        add(new Expertise());
        add(new Concentrate());

        return uglyThing;
    }

    private static void add(AbstractCard card) {
        uglyThing.put(card.cardID, uglyThing.size());
    }
}
