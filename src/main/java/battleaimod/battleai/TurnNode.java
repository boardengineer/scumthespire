package battleaimod.battleai;

import battleaimod.BattleAiMod;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.cards.colorless.RitualDagger;
import com.megacrit.cardcrawl.cards.green.Catalyst;
import com.megacrit.cardcrawl.cards.purple.ConjureBlade;
import com.megacrit.cardcrawl.cards.purple.LessonLearned;
import com.megacrit.cardcrawl.cards.red.Feed;
import com.megacrit.cardcrawl.cards.tempCards.Expunger;
import com.megacrit.cardcrawl.cards.tempCards.Miracle;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.exordium.GremlinNob;
import com.megacrit.cardcrawl.monsters.exordium.Lagavulin;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.LizardTail;
import ludicrousspeed.simulator.commands.Command;
import ludicrousspeed.simulator.commands.EndCommand;
import savestate.CardState;
import savestate.PotionState;
import savestate.SaveState;
import savestate.relics.RelicState;

import java.util.*;
import java.util.stream.Collectors;

import static savestate.SaveStateMod.addRuntime;

public class TurnNode implements Comparable<TurnNode> {
    public final BattleAiController controller;
    public final int turnLabel;
    public Stack<StateNode> states;
    public boolean runningCommands = false;
    public boolean isDone = false;

    public StateNode startingState;
    private boolean initialized = false;
    int turnIndex = 0;

    public List<TurnNode> children;
    public TurnNode parent;
    private Optional<Integer> cachedValue = Optional.empty();

    static int nodeIndex = 0;


    public TurnNode(StateNode statenode, BattleAiController controller, TurnNode parent) {
        startingState = statenode;
        this.controller = controller;
        this.parent = parent;
        this.turnLabel = nodeIndex++;
        children = new ArrayList<>();

        if (parent != null && BattleAiController.SHOULD_SHOW_TREE) {
            parent.children.add(this);
        }
    }

    public boolean step() {
        long stepStart = System.currentTimeMillis();

        if (isDone) {
            return true;
        }

        if (!initialized) {
            initialized = true;
            states = new Stack<>();
            states.push(startingState);
        }

        if (states.isEmpty()) {
            isDone = true;
            return true;
        }

        StateNode curState = states.peek();
        if (!runningCommands) {
            runningCommands = true;
            curState.saveState.loadState();
            addRuntime("Battle AI TurnStep Checkpoint 0", System
                    .currentTimeMillis() - stepStart);
            return false;
        }

        if (AbstractDungeon.player.currentHealth < 1) {
            curState.saveState = new SaveState();
            if (controller.deathNode == null ||
                    (controller.deathNode != null && controller.deathNode.saveState.turn < curState.saveState.turn)) {
//                System.err.println("dead 3");
                controller.deathNode = curState;
            }
//            isDone = true;
//            return true;
        }

        addRuntime("Battle AI TurnStep Checkpoint 1", System
                .currentTimeMillis() - stepStart);

        if (curState != startingState && isNewTurn(curState)) {
            if (curState.saveState == null) {
                curState.saveState = new SaveState();
            }


            controller.turnsLoaded++;
            addRuntime("turnsLoaded", 1);
            TurnNode toAdd = new TurnNode(curState, controller, this);
            states.pop();

            while (!states.isEmpty() && states.peek().isDone()) {
                states.pop();
            }
            addRuntime("Battle AI TurnStep early Return Total 1", System
                    .currentTimeMillis() - stepStart);

            runningCommands = false;
//            if (curState.lastCommand instanceof EndCommand) {
//                ((EndCommand) curState.lastCommand).stateDebugInfo = new StateDebugInfo(curState.saveState);
//            }

            addRuntime("Battle AI TurnStep early Return Total 2", System
                    .currentTimeMillis() - stepStart);

            int turnNumber = GameActionManager.turn;
            if (AbstractDungeon.player.currentHealth >= 1) {
                if (turnNumber >= controller.targetTurn) {
                    if (controller.bestTurn == null || toAdd.isBetterThan(controller.bestTurn)) {
                        controller.bestTurn = toAdd;
                    }
                } else {
                    if (controller.backupTurn == null ||
                            controller.backupTurn.startingState.saveState.turn < toAdd.startingState.saveState.turn ||
                            (toAdd.isBetterThan(controller.backupTurn)) && controller.backupTurn.startingState.saveState.turn == toAdd.startingState.saveState.turn) {
                        controller.backupTurn = toAdd;
                    }
//                    System.err.println("adding " + toAdd);

                    long startAdd = System.currentTimeMillis();

                    controller.turns.add(toAdd);

                    addRuntime("Battle AI TurnStep Add Turn", System
                            .currentTimeMillis() - startAdd);
                }
            }

            turnIndex++;

            addRuntime("Battle AI TurnStep early Return Total", System
                    .currentTimeMillis() - stepStart);

            return true;
        }
        addRuntime("Battle AI TurnStep Checkpoint 2", System
                .currentTimeMillis() - stepStart);

        if (curState.isDone()) {
            states.pop();
            if (!states.empty()) {
                states.peek().saveState.loadState();
            }
        } else {
            long startNodeSep = System.currentTimeMillis();

            Command toExecute = curState.step();

            addRuntime("Battle AI StateNode Step", System
                    .currentTimeMillis() - startNodeSep);

            if (toExecute == null) {
                states.pop();
                if (!states.isEmpty()) {
                    states.peek().saveState.loadState();
                }
            } else {
//                System.err.println("adding node for " + toExecute);
                StateNode toAdd = new StateNode(curState, toExecute, controller);

                try {
                    long startExecute = System.currentTimeMillis();
                    toExecute.execute();
                    states.push(toAdd);

                    addRuntime("Battle AI Execute Action", System
                            .currentTimeMillis() - startExecute);
                } catch (IndexOutOfBoundsException e) {
                    addRuntime("Execution Exception", 1);
//                    e.printStackTrace();
//                    System.err.println("desynced state, aborting state to recover");
                }
            }
        }

        addRuntime("Battle AI TurnStep Checkpoint 3", System
                .currentTimeMillis() - stepStart);

        if (states.isEmpty()) {
            isDone = true;
        }

        return false;
    }

    @Override
    public String toString() {
        int monsterDamage = getTotalMonsterHealth(controller.startingState) - getTotalMonsterHealth(startingState.saveState);

        return String
                .format("hp:%03d ehp:%03d (%03d) turn:%02d score:%4d", getPlayerDamage(this), getTotalMonsterHealth(this), monsterDamage, startingState.saveState.turn, getTurnScore(this));
    }

    public static int getTotalMonsterHealth(TurnNode turnNode) {
        return getTotalMonsterHealth(turnNode.startingState.saveState);
    }

    public static int getPlayerDamage(TurnNode turnNode) {
        return StateNode.getPlayerDamage(turnNode.startingState);
    }

    public static int getNumSlimes(TurnNode turnNode) {
        return turnNode.startingState.saveState.getNumSlimes();
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

    public static int getRelicScore(SaveState saveState) {
        Optional<RelicState> optionalLizardTail = saveState.playerState.relics.stream()
                                                                              .filter(relic -> relic.relicId
                                                                                      .equals(LizardTail.ID) && relic.counter != -2)
                                                                              .findAny();
        return optionalLizardTail.isPresent() ? 400 : 0;
    }

    public static int getTurnScore(TurnNode turnNode) {
        if (!turnNode.cachedValue.isPresent()) {
            turnNode.cachedValue = Optional.of(caclculateTurnScore(turnNode));
        }
        return turnNode.cachedValue.get();
    }

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
        int monsterDamage = getTotalMonsterHealth(turnNode.controller.startingState) - getTotalMonsterHealth(turnNode.startingState.saveState);

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

    @Override
    public int compareTo(TurnNode otherTurn) {
        long startCompare = System.currentTimeMillis();

        int result = getTurnScore(otherTurn) - getTurnScore(this);

        addRuntime("Comparing Turns", System.currentTimeMillis() - startCompare);
        addRuntime("Comparing Turns instance", 1);

        return result;
    }

    public boolean isBetterThan(TurnNode other) {
        return compareTo(other) < 0;
    }

    private boolean isNewTurn(StateNode childNode) {
        return (GameActionManager.turn > this.startingState.saveState.turn) || childNode.lastCommand instanceof EndCommand;
    }

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
}
