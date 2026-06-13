package battleaimod.battleai.evolution.utils;

import basemod.ReflectionHacks;
import battleaimod.ValueFunctions;
import battleaimod.utils.FileLogger;
import com.megacrit.cardcrawl.cards.AbstractCard;
import savestate.SaveState;
import savestate.monsters.MonsterState;
import savestate.powers.PowerState;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueFunctionManager {

    //IMPORTANT: DO NOT CHANGE THE ORDER OF THESE
    //PLEASE APPEND NEW VARIABLES ONLY TO THE END
    public enum Variables{
        SUM_DAMAGE_DEALT,
        DAMAGE_RECEIVED,
        MONSTERS_REMAINING,
        SUM_MONSTER_HEALTH,
        AVG_MONSTER_HEALTH,
        POWERS_PLAYED, //now is the sum of energy cost of powers played
        SUM_ENEMY_POISON,
        SUM_ENEMY_WEAK,
        SUM_ENEMY_VULNERABLE,
        SUM_ENEMY_STRENGTH,
        SUM_PLAYER_WEAK,
        SUM_PLAYER_VULNERABLE,
        SUM_PLAYER_STRENGTH,
        SUM_PLAYER_DEX,
        EFFECTIVE_HP,
        SUM_ORBS,
        HAND_SPACE_CLOG,
        SUM_CURSE_EXHAUSTED,
        SUM_CARD_EXHAUSTED,
        PLAYER_DEATH,
        MIN_MONSTER_HEALTH,
        MAX_MONSTER_HEALTH,
        MAX_ENEMY_POISON,
        ;
    }


    private static SaveState startState;
    private static SaveState endState;
    private static List<AbstractCard> cardsPlayed;
    private static List<AbstractCard> endHand;

    private static final Map<String, Integer> monsterPowerTotals = new HashMap<>();
    private static boolean monsterCacheValid = false;
    private static final Map<String, Integer> playerPowerTotals = new HashMap<>();
    private static boolean playerCacheValid = false;

    private static final Map<Variables, Double> valueMap = new HashMap<>();

    public static void writeVariablesToFile(String fileName) {
        File file = new File(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            for (Variables v : Variables.values()) {
                writer.write(v.name());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Variables to file", e);
        }
    }

    public static void initFuncValues(SaveState startState, SaveState endState, List<AbstractCard> cardsPlayed, List<AbstractCard> endHand){
        ValueFunctionManager.startState = startState;
        ValueFunctionManager.endState = endState;
        ValueFunctionManager.cardsPlayed = cardsPlayed;
        ValueFunctionManager.endHand = endHand;

        ValueFunctionManager.monsterCacheValid = false;
        ValueFunctionManager.playerCacheValid = false;

        addValueToMap(Variables.SUM_DAMAGE_DEALT, getTotalDamageDealt());

        addValueToMap(Variables.SUM_MONSTER_HEALTH, getTotalMonsterHealth());

        addValueToMap(Variables.AVG_MONSTER_HEALTH, getAverageMonsterHealth());

        addValueToMap(Variables.MONSTERS_REMAINING, getAliveMonsterCount());

        addValueToMap(Variables.DAMAGE_RECEIVED, getPlayerDamage());

        addValueToMap(Variables.POWERS_PLAYED, getNumberPowersPlayedCost());

        addValueToMap(Variables.SUM_ENEMY_POISON, getMonsterPowerTotal("Poison"));
        addValueToMap(Variables.SUM_ENEMY_WEAK, getMonsterPowerTotal("Weakened"));
        addValueToMap(Variables.SUM_ENEMY_VULNERABLE, getMonsterPowerTotal("Vulnerable"));
        addValueToMap(Variables.SUM_ENEMY_STRENGTH, getMonsterPowerTotal("Strength"));

        addValueToMap(Variables.SUM_PLAYER_WEAK, getPlayerPowerTotal("Weakened"));
        addValueToMap(Variables.SUM_PLAYER_VULNERABLE, getPlayerPowerTotal("Vulnerable"));
        addValueToMap(Variables.SUM_PLAYER_STRENGTH, getPlayerPowerTotal("Strength"));
        addValueToMap(Variables.SUM_PLAYER_DEX, getPlayerPowerTotal("Dexterity"));

        addValueToMap(Variables.EFFECTIVE_HP, getPlayerEffectiveHP());
        addValueToMap(Variables.SUM_ORBS, getSumOrbs());

        addValueToMap(Variables.HAND_SPACE_CLOG, getHandSpaceClog());

        addValueToMap(Variables.SUM_CARD_EXHAUSTED, getSumCardExhausted());
        addValueToMap(Variables.SUM_CURSE_EXHAUSTED, getSumCurseExhausted());

        addValueToMap(Variables.PLAYER_DEATH, getPlayerDeath());

        addValueToMap(Variables.MIN_MONSTER_HEALTH, getMinMonsterHealth());
        addValueToMap(Variables.MAX_MONSTER_HEALTH, getMaxMonsterHealth());

        addValueToMap(Variables.MAX_ENEMY_POISON, getMonsterPowerMax("Poison"));
    }

    private static void addValueToMap(Variables v, double d){
        ValueFunctionManager.valueMap.put(v, d);
    }

    /**
     * Counts the number of monsters currently alive.
     * Awakened One still counts as alive before its second form, even at 0 HP.
     */
    public static int getAliveMonsterCount(SaveState state) {
        if (state == null ||
                state.curMapNodeState == null ||
                state.curMapNodeState.monsterData == null) {
            return 0;
        }

        return (int) state.curMapNodeState.monsterData.stream()
                .filter(monster -> monster != null && isMonsterEffectivelyAlive(monster))
                .count();
    }

    public static int getAliveMonsterCount() {
        return getAliveMonsterCount(endState);
    }

    private static boolean isMonsterEffectivelyAlive(MonsterState monster) {
        if (monster.currentHealth > 0) {
            return true;
        }

        return "AwakenedOne".equals(monster.id) && isAwakenedOneNotAwakened(monster);
    }

    /**
     * Adds up monster health and accounts for powers that alter the effective health of the enemy
     * such as barricade and unawakened.
     */
    public static int getTotalMonsterHealth() {
        return getTotalMonsterHealth(endState);
    }

    //This is so we can call it from evo manager
    public static int getTotalMonsterHealth(SaveState s) {
        return s.curMapNodeState.monsterData.stream()
                .map(monster -> {
                    if (monster.powers.stream()
                            .anyMatch(power -> power.powerId
                                    .equals("Barricade"))) {
                        return monster.currentHealth + monster.currentBlock;
                    } else if ("AwakenedOne".equals(monster.id) && isAwakenedOneNotAwakened(monster)) {
                        FileLogger.log("AwakenedOne is NOT awakened, adding health!");
                        return monster.currentHealth + monster.maxHealth;
                    }
                    return monster.currentHealth;
                })
                .reduce(Integer::sum)
                .get();
    }

    public static double getAverageMonsterHealth() {
        return getAverageMonsterHealth(endState);
    }

    public static double getAverageMonsterHealth(SaveState s) {
        int size = getAliveMonsterCount();
        return size > 0 ? getTotalMonsterHealth() / (double) size : 0.0;
    }

    private static boolean isAwakenedOneNotAwakened(MonsterState state) {

        byte nextMove = getMonsterStateNextMove(state);

        return nextMove != 5 && // DARK_ECHO
                nextMove != 6 && // SLUDGE
                nextMove != 8;   // TACKLE
    }

    private static byte getMonsterStateNextMove(MonsterState state) {
        return ReflectionHacks.getPrivate(
                state,
                MonsterState.class,
                "nextMove"
        );
    }


    public static int getTotalDamageDealt() {
        return ValueFunctionManager.getTotalMonsterHealth(startState)
                - ValueFunctionManager.getTotalMonsterHealth(endState);
    }

    public static int getPlayerDamage() {
        return startState.getPlayerHealth() - endState.getPlayerHealth();
    }

    public static double getVariableValue(Variables var){
        return valueMap.getOrDefault(var, 0.0);
    }

    public static int getNumberPowersPlayedCost(){
        int sum = 0;
        for(AbstractCard c : cardsPlayed){
            if(c.type == AbstractCard.CardType.POWER){
                sum += c.cost;
            }
        }
        return sum;
    }



    private static void computePlayerPowerTotals() {
        playerPowerTotals.clear();

        for (PowerState p : endState.playerState.powers) {
            playerPowerTotals.merge(p.powerId, p.amount, Integer::sum);
        }

        playerCacheValid = true;
    }

    public static int getPlayerPowerTotal(String powerId) {
        if (!playerCacheValid) computePlayerPowerTotals();
        return playerPowerTotals.getOrDefault(powerId, 0);
    }

    private static void computeMonsterPowerTotals() {
        monsterPowerTotals.clear();

        if (endState == null ||
                endState.curMapNodeState == null ||
                endState.curMapNodeState.monsterData == null) {
            monsterCacheValid = true;
            return;
        }

        for (MonsterState m : endState.curMapNodeState.monsterData) {
            for (PowerState p : m.powers) {
                monsterPowerTotals.merge(p.powerId, p.amount, Integer::sum);
            }
        }

        monsterCacheValid = true;
    }

    public static int getMonsterPowerTotal(String powerId) {
        if (!monsterCacheValid) computeMonsterPowerTotals();
        return monsterPowerTotals.getOrDefault(powerId, 0);
    }

    private static final Map<String, Integer> monsterPowerMax = new HashMap<>();
    private static boolean monsterMaxCacheValid = false;

    private static void computeMonsterPowerMax() {
        monsterPowerMax.clear();

        if (endState == null ||
                endState.curMapNodeState == null ||
                endState.curMapNodeState.monsterData == null) {
            monsterMaxCacheValid = true;
            return;
        }

        for (MonsterState m : endState.curMapNodeState.monsterData) {
            for (PowerState p : m.powers) {
                monsterPowerMax.merge(p.powerId, p.amount, Math::max);
            }
        }

        monsterMaxCacheValid = true;
    }

    public static int getMonsterPowerMax(String powerId) {
        if (!monsterMaxCacheValid) computeMonsterPowerMax();
        return monsterPowerMax.getOrDefault(powerId, 0);
    }

    public static int getPlayerEffectiveHP(){
        boolean barricade = false;
        for(PowerState p : endState.playerState.powers){
            if(p.powerId.equals("Barricade")){
                barricade = true;
                break;
            }
        }

        return endState.playerState.getCurrentHealth() + (barricade ? endState.playerState.currentBlock : 0);
    }

    public static int getSumOrbs(){
        return endState.playerState.orbs.size();
    }

    public static int getHandSpaceClog(){
        int sum = 0;
        for (AbstractCard c : endHand){
            if(c.type == AbstractCard.CardType.CURSE ||
                    c.type == AbstractCard.CardType.STATUS ||
                    c.cost == -2){
                sum++;
            }
        }
        return sum;
    }

    public static int getSumCardExhausted() {
        int count = countCardsInExhaust(endState, false);
        //FileLogger.log("Final SUM_CARD_EXHAUSTED count: " + count);
        return count;
    }

    public static int getSumCurseExhausted() {
        int count = countCardsInExhaust(endState, true);
        //FileLogger.log("Final SUM_CURSE_EXHAUSTED count: " + count);
        return count;
    }

    private static int countCardsInExhaust(SaveState state, boolean countCursesAndStatuses) {
        if (state == null || state.playerState == null || state.playerState.exhaustPile == null) {
            //FileLogger.logWarning("Exhaust pile, playerState, or state is null. Returning 0.");
            return 0;
        }

        int count = 0;
        //FileLogger.log("--- Scanning Exhaust Pile (Targeting Curses/Statuses: " + countCursesAndStatuses + ") ---");

        for (savestate.CardState cardState : state.playerState.exhaustPile) {

            // 1. Load the actual card object
            AbstractCard actualCard = cardState.loadCard();

            // 2. Check the type
            AbstractCard.CardType type = actualCard.type;
            boolean isCurseOrStatus = (type == AbstractCard.CardType.CURSE || type == AbstractCard.CardType.STATUS);

            if (countCursesAndStatuses && isCurseOrStatus) {
                //FileLogger.log("  [+] Counted Curse/Status: " + actualCard.name);
                count++;
            } else if (!countCursesAndStatuses && !isCurseOrStatus) {
                //FileLogger.log("  [+] Counted Standard Card: " + actualCard.name);
                count++;
            } else {
                //FileLogger.log("  [-] Skipped: " + actualCard.name + " (Type: " + type.name() + ")");
            }

            // 3. CRITICAL FOR AI: Free the card to prevent memory leaks during simulations
            savestate.CardState.freeCard(actualCard);
        }

        //FileLogger.log("--- Finished Scanning. Total Found: " + count + " ---");
        return count;
    }

    public static int getPlayerDeath(){
        return endState.playerState.currentHealth <= 0 ? 1 : 0;
    }

    public static int getMinMonsterHealth() {
        return getMinMonsterHealth(endState);
    }

    public static int getMinMonsterHealth(SaveState s) {
        if (s == null ||
                s.curMapNodeState == null ||
                s.curMapNodeState.monsterData == null ||
                s.curMapNodeState.monsterData.isEmpty()) {
            return 0;
        }

        return s.curMapNodeState.monsterData.stream()
                .map(monster -> {
                    if (monster.powers.stream()
                            .anyMatch(power -> power.powerId.equals("Barricade"))) {
                        return monster.currentHealth + monster.currentBlock;
                    } else if ("AwakenedOne".equals(monster.id) && isAwakenedOneNotAwakened(monster)) {
                        return monster.currentHealth + monster.maxHealth;
                    }
                    return monster.currentHealth;
                })
                .min(Integer::compareTo)
                .orElse(0);
    }

    public static int getMaxMonsterHealth() {
        return getMaxMonsterHealth(endState);
    }

    public static int getMaxMonsterHealth(SaveState s) {
        if (s == null ||
                s.curMapNodeState == null ||
                s.curMapNodeState.monsterData == null ||
                s.curMapNodeState.monsterData.isEmpty()) {
            return 0;
        }

        return s.curMapNodeState.monsterData.stream()
                .map(monster -> {
                    if (monster.powers.stream()
                            .anyMatch(power -> power.powerId.equals("Barricade"))) {
                        return monster.currentHealth + monster.currentBlock;
                    } else if ("AwakenedOne".equals(monster.id) && isAwakenedOneNotAwakened(monster)) {
                        return monster.currentHealth + monster.maxHealth;
                    }
                    return monster.currentHealth;
                })
                .max(Integer::compareTo)
                .orElse(0);
    }
}
