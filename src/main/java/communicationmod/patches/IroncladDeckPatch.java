package communicationmod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.red.*;
import com.megacrit.cardcrawl.helpers.CardLibrary;

import static com.megacrit.cardcrawl.helpers.CardLibrary.add;

public class IroncladDeckPatch {
    @SpirePatch(
            clz = CardLibrary.class,
            paramtypez = {},
            method = "addRedCards"
    )
    public static class SlowAttackAnimationPatch {
        public static void Replace(CardLibrary _instance) {
            // Starter Cards
            add(new Defend_Red());
            add(new Strike_Red());

            // Necessary for Unlocks
            add(new Evolve());
            add(new Exhume());
            add(new Havoc());
            add(new HeavyBlade());
            add(new Immolate());
            add(new LimitBreak());
            add(new Sentinel());
            add(new SpotWeakness());
            add(new WildStrike());


            add(new Anger());
            add(new Armaments());
            add(new Barricade());
            add(new Bash());
            add(new BattleTrance());
            add(new Berserk());
//            add(new BloodForBlood());
//            add(new Bloodletting());
            add(new Bludgeon());
//            add(new BodySlam());
//            add(new Brutality());
//            add(new BurningPact());
            add(new Carnage());
//            add(new Clash());
            add(new Cleave());
            add(new Clothesline());
//            add(new Combust());
//            add(new Corruption());
//            add(new DarkEmbrace());
//            add(new DemonForm());
            add(new Disarm());
            add(new DoubleTap());
            add(new Dropkick());
//            add(new DualWield());
//            add(new Entrench());
//            add(new Feed());
//            add(new FeelNoPain());
            add(new FiendFire());
//            add(new FireBreathing());
            add(new FlameBarrier());
//            add(new Flex());
            add(new GhostlyArmor());

//            add(new Headbutt());
            add(new Hemokinesis());
            add(new Impervious());
//            add(new InfernalBlade());
            add(new Inflame());
//            add(new Intimidate());
            add(new IronWave());
            add(new Juggernaut());
            add(new Metallicize());
            add(new Offering());
            add(new PerfectedStrike());
            add(new PommelStrike());
            add(new PowerThrough());
            add(new Pummel());
//            add(new Rage());
            add(new Rampage());
            add(new Reaper());
            add(new RecklessCharge());
//            add(new Rupture());
            add(new SearingBlow());
//            add(new SecondWind());
            add(new SeeingRed());
            add(new SeverSoul());
            add(new Shockwave());
            add(new ShrugItOff());

            add(new SwordBoomerang());
            add(new ThunderClap());
//            add(new TrueGrit());
            add(new TwinStrike());
            add(new Uppercut());
//            add(new Warcry());
            add(new Whirlwind());
        }
    }
}
