package battleaimod.utils;

import com.megacrit.cardcrawl.powers.AbstractPower;

public class FitnessTracker {

    public static int totalPoison = 0;
    public static int totalWeak = 0;
    public static int totalVuln = 0;

    public static void reset() {
        totalPoison = 0;
        totalWeak = 0;
        totalVuln = 0;
    }

    public static void onPowerApply(AbstractPower power) {
        if (power == null) return;

        switch (power.ID) {
            case "Poison":
                totalPoison += power.amount;
                break;
            case "Weak":
                totalWeak += power.amount;
                break;
            case "Vulnerable":
                totalVuln += power.amount;
                break;
        }
    }
}
