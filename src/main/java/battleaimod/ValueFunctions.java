package battleaimod;

import battleaimod.battleai.StateNode;
import battleaimod.battleai.TurnNode;
import com.megacrit.cardcrawl.cards.colorless.RitualDagger;
import com.megacrit.cardcrawl.cards.green.Catalyst;
import com.megacrit.cardcrawl.cards.purple.ConjureBlade;
import com.megacrit.cardcrawl.cards.purple.LessonLearned;
import com.megacrit.cardcrawl.cards.red.Feed;
import com.megacrit.cardcrawl.cards.tempCards.Expunger;
import com.megacrit.cardcrawl.cards.tempCards.Miracle;
import com.megacrit.cardcrawl.monsters.exordium.GremlinNob;
import com.megacrit.cardcrawl.monsters.exordium.Lagavulin;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.LizardTail;
import savestate.CardState;
import savestate.PotionState;
import savestate.SaveState;
import savestate.relics.RelicState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class ValueFunctions {

    public static HashSet<String> BRAWLY_MONSTER_IDS = new HashSet<String>() {{
        add(Lagavulin.ID);
        add(GremlinNob.ID);
    }};

    public static final HashMap<String, Integer> POWER_VALUES = new HashMap<String, Integer>() {{
        put(StrengthPower.POWER_ID, 3);
        put(DexterityPower.POWER_ID, 3);
        put(FocusPower.POWER_ID, 3);
        put(DarkEmbracePower.POWER_ID, 4);
        put(FeelNoPainPower.POWER_ID, 5);
        put(DemonFormPower.POWER_ID, 6);
        put(FireBreathingPower.POWER_ID, 2);
        put(CombustPower.POWER_ID, 2);
        put(EvolvePower.POWER_ID, 16);

        put(AccuracyPower.POWER_ID, 3);
        put(EchoPower.POWER_ID, 40);
    }};

    /**
     * Adds up monster health and accounts for powers that alter the effective health of the enemy
     * such as barricade and unawakened.
     */
    public static int getTotalMonsterHealth(SaveState saveState) {
        return saveState.curMapNodeState.monsterData.stream()
                                                    .map(monster -> {
                                                        if (monster.powers.stream()
                                                                          .filter(power -> power.powerId
                                                                                  .equals("Barricade"))
                                                                          .findAny().isPresent()) {
                                                            return monster.currentHealth + monster.currentBlock;
                                                        } else if (monster.powers.stream()
                                                                                 .filter(power -> power.powerId
                                                                                         .equals("Unawakened"))
                                                                                 .findAny()
                                                                                 .isPresent()) {
                                                            return monster.currentHealth + monster.maxHealth;
                                                        }
                                                        return monster.currentHealth;
                                                    })
                                                    .reduce(Integer::sum)
                                                    .get();
    }

    public static int caclculateTurnScore(TurnNode turnNode) {
        int playerDamage = getPlayerDamage(turnNode);
        int monsterDamage = ValueFunctions
                .getTotalMonsterHealth(turnNode.controller.startingState) - ValueFunctions
                .getTotalMonsterHealth(turnNode.startingState.saveState);

        int powerScore = turnNode.startingState.saveState.playerState.powers.stream()
                                                                            .map(powerState -> POWER_VALUES
                                                                                    .getOrDefault(powerState.powerId, 0) * powerState.amount)
                                                                            .reduce(Integer::sum)
                                                                            .orElse(0);


        boolean shouldBrawl = turnNode.startingState.saveState.curMapNodeState.monsterData.stream()
                                                                                          .filter(monsterState -> BRAWLY_MONSTER_IDS
                                                                                                  .contains(monsterState.id))
                                                                                          .findAny()
                                                                                          .isPresent();

        int numRitualDaggers = 0;
        int totalRitualDaggerDamage = 0;
        int numMiracles = 0;
        int numFeeds = 0;
        int numCatalysts = 0;

        int numLessonLearned = 0;

        int numConjures = 0;
        int conjureDamage = 0;

        for (CardState card : turnNode.startingState.saveState.playerState.hand) {
            switch (card.cardId) {
                case RitualDagger.ID:
                    numRitualDaggers++;
                    totalRitualDaggerDamage += card.baseDamage;
                    break;
                case Miracle.ID:
                    numMiracles++;
                    break;
                case Feed.ID:
                    numFeeds++;
                    break;
                case ConjureBlade.ID:
                    numConjures++;
                    break;
                case Expunger.ID:
                    conjureDamage += card.baseMagicNumber;
                    break;
                case LessonLearned.ID:
                    numLessonLearned++;
                    break;
                case Catalyst.ID:
                    numCatalysts++;
                    break;
                default:
                    break;
            }
        }

        for (CardState card : turnNode.startingState.saveState.playerState.drawPile) {
            switch (card.cardId) {
                case RitualDagger.ID:
                    numRitualDaggers++;
                    totalRitualDaggerDamage += card.baseDamage;
                    break;
                case Feed.ID:
                    numFeeds++;
                    break;
                case ConjureBlade.ID:
                    numConjures++;
                    break;
                case Expunger.ID:
                    conjureDamage += card.baseMagicNumber;
                    break;
                case LessonLearned.ID:
                    numLessonLearned++;
                    break;
                case Catalyst.ID:
                    numCatalysts++;
                    break;
                default:
                    break;
            }
        }

        for (CardState card : turnNode.startingState.saveState.playerState.discardPile) {
            switch (card.cardId) {
                case RitualDagger.ID:
                    numRitualDaggers++;
                    totalRitualDaggerDamage += card.baseDamage;
                    break;
                case Feed.ID:
                    numFeeds++;
                    break;
                case ConjureBlade.ID:
                    numConjures++;
                    break;
                case Expunger.ID:
                    conjureDamage += card.baseMagicNumber;
                    break;
                case LessonLearned.ID:
                    numLessonLearned++;
                    break;
                case Catalyst.ID:
                    numCatalysts++;
                    break;
                default:
                    break;
            }
        }

        for (CardState card : turnNode.startingState.saveState.playerState.exhaustPile) {
            switch (card.cardId) {
                case RitualDagger.ID:
                    totalRitualDaggerDamage += card.baseDamage;
                    break;
                default:
                    break;
            }
        }

        int miracleScore = numMiracles * 20;
        int ritualDaggerScore = numRitualDaggers * 40 + totalRitualDaggerDamage * 80;
        int feedScore = numFeeds * 40 + turnNode.startingState.saveState.playerState.maxHealth * 30;
        int conjureBladeScore = numConjures * 25 + (conjureDamage * 15);
        int lessonLearnedScore = numLessonLearned * 40 + turnNode.startingState.saveState.lessonLearnedCount * 200;
        int parasiteScore = turnNode.startingState.saveState.parasiteCount * -80;
        int catalystScore = numCatalysts * 25;

        int healthMultiplier = shouldBrawl ? 2 : 8;
        int numOrbScore = turnNode.startingState.saveState.playerState.maxOrbs == 0 ? -1000 : 0;

        int additonalHeuristicScore =
                BattleAiMod.additionalValueFunctions.stream()
                                                    .map(function -> function
                                                            .apply(turnNode.startingState.saveState))
                                                    .collect(Collectors
                                                            .summingInt(Integer::intValue));

        return numOrbScore + catalystScore + parasiteScore + lessonLearnedScore + feedScore + conjureBladeScore + turnNode.startingState.saveState.playerState.gold * 2 + ritualDaggerScore + miracleScore + monsterDamage - healthMultiplier * playerDamage + powerScore + getPotionScore(turnNode.startingState.saveState) + getRelicScore(turnNode.startingState.saveState) + additonalHeuristicScore;
    }

    public static int getPlayerDamage(TurnNode turnNode) {
        return StateNode.getPlayerDamage(turnNode.startingState);
    }

    public static int getPotionScore(SaveState saveState) {
        return saveState.playerState.potions.stream().map(potionState -> {
            if (potionState.potionId.equals("Potion Slot") || !PotionState.POTION_VALUES
                    .containsKey(potionState.potionId)) {
                return 0;
            }
            return PotionState.POTION_VALUES.get(potionState.potionId);
        }).reduce(Integer::sum).get();
    }

    /**
     * This is used for end of battle score.  Only effects that last between battles such as health,
     * potions, and scaling effects matter here.
     */
    public static int getStateScore(StateNode node) {
        int totalRitualDaggerDamage = 0;
        for (CardState card : node.saveState.playerState.hand) {
            switch (card.cardId) {
                case RitualDagger.ID:
                    totalRitualDaggerDamage += card.baseDamage;
                    break;
                default:
                    break;
            }
        }

        for (CardState card : node.saveState.playerState.drawPile) {
            switch (card.cardId) {
                case RitualDagger.ID:
                    totalRitualDaggerDamage += card.baseDamage;
                    break;
                default:
                    break;
            }
        }

        for (CardState card : node.saveState.playerState.discardPile) {
            switch (card.cardId) {
                case RitualDagger.ID:
                    totalRitualDaggerDamage += card.baseDamage;
                    break;
                default:
                    break;
            }
        }

        for (CardState card : node.saveState.playerState.exhaustPile) {
            switch (card.cardId) {
                case RitualDagger.ID:
                    totalRitualDaggerDamage += card.baseDamage;
                    break;
                default:
                    break;
            }
        }

        int ritualDaggerScore = totalRitualDaggerDamage * 80;
        int lessonLearnedScore = node.saveState.lessonLearnedCount * 100;
        int feedScore = node.saveState.playerState.maxHealth * 30;

        int additonalHeuristicScore =
                BattleAiMod.additionalValueFunctions.stream()
                                                    .map(function -> function
                                                            .apply(node.saveState))
                                                    .collect(Collectors
                                                            .summingInt(Integer::intValue));

        return feedScore +
                node.saveState.playerState.gold * 2 +
                ritualDaggerScore +
                StateNode.getPlayerDamage(node) * -1 +
                ValueFunctions.getPotionScore(node.saveState) +
                getRelicScore(node.saveState) +
                lessonLearnedScore +
                additonalHeuristicScore;
    }

    public static int getRelicScore(SaveState saveState) {
        Optional<RelicState> optionalLizardTail = saveState.playerState.relics.stream()
                                                                              .filter(relic -> relic.relicId
                                                                                      .equals(LizardTail.ID) && relic.counter != -2)
                                                                              .findAny();
        return optionalLizardTail.isPresent() ? 400 : 0;
    }
}
