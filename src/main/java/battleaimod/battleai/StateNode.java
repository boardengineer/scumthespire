package battleaimod.battleai;

import battleaimod.BattleAiMod;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.RitualDagger;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import ludicrousspeed.simulator.commands.Command;
import ludicrousspeed.simulator.commands.CommandList;
import ludicrousspeed.simulator.commands.EndCommand;
import savestate.CardState;
import savestate.SaveState;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StateNode {
    private final BattleAiController controller;
    public final StateNode parent;
    public final Command lastCommand;
    public String stateString;
    public SaveState saveState;

    private int minDamage = 5000;
    public List<Command> commands;
    private boolean initialized = false;
    private int commandIndex = -1;
    private boolean isDone = false;
    int turnDepth;

    public StateNode(StateNode parent, Command lastCommand, BattleAiController controller) {
        this.parent = parent;
        this.lastCommand = lastCommand;
        this.controller = controller;
    }

    /**
     * Does the next step and returns true iff the parent should load state
     */
    public Command step() {
        if (saveState == null) {
            saveState = new SaveState();
        }

        if (parent == null || parent.saveState.turn < saveState.turn) {
            turnDepth = 0;
        } else {
            turnDepth = parent.turnDepth + 1;
        }

        if (commands == null) {
            populateCommands();
        }

        if (turnDepth > 50) {
            boolean hasEnd = false;
            for (Command command : commands) {
                if (command instanceof EndCommand) {
                    hasEnd = true;
                    break;
                }
            }

            if (hasEnd) {
                commands.clear();
                commands.add(new EndCommand());
            }


        }

        if (!initialized) {
            initialized = true;

            if (AbstractDungeon.player.isDead || AbstractDungeon.player.isDying) {
                if (controller.deathNode == null ||
                        (controller.deathNode != null && controller.deathNode.saveState.turn < saveState.turn)) {
                    System.err.println("dead 1");
                    controller.deathNode = this;
                    isDone = true;
                    return null;
                }
            }

            int damage = controller.startingHealth - saveState.getPlayerHealth();

            boolean isBattleWon = isBattleOver();
            if (!isBattleWon && damage < (controller.minDamage + 6)) {
                commandIndex = 0;
            } else {
//                System.err
//                        .printf("Found terminal state on init: damage this combat:%s; best damage: %s\n", damage, controller.minDamage);

                if (isBattleWon) {
                    if (controller.bestEnd == null || (getStateScore(this) > getStateScore(controller.bestEnd))
                            && saveState.getPlayerHealth() >= 1) {
                        controller.minDamage = damage;
                        controller.bestEnd = this;
                    }
                } else if (AbstractDungeon.player.isDead || AbstractDungeon.player.isDying) {
                    if (controller.deathNode == null ||
                            (controller.deathNode != null && controller.deathNode.saveState.turn < saveState.turn)) {
                        System.err.println("dead 2");
                        controller.deathNode = this;
                        isDone = true;
                    }
                }

                minDamage = damage;
                isDone = true;
                return null;
            }
        }

        if (commands.size() == 0) {
            isDone = true;
            return null;
        }


        Command toExecute = commands.get(commandIndex);
        commandIndex++;
        isDone = commandIndex >= commands.size();

        return toExecute;
    }

    private boolean isBattleOver() {
        return AbstractDungeon.getCurrRoom().monsters.areMonstersBasicallyDead();
    }

    private void populateCommands() {
        Comparator<AbstractCard> combinedPlayHeuristic = (card1, card2) -> {
            for (Comparator<AbstractCard> heuristic : BattleAiMod.cardPlayHeuristics) {
                int heuristicResult = heuristic.compare(card1, card2);
                if (heuristicResult != 0) {
                    return heuristicResult;
                }
            }
            return 0;
        };

        commands = CommandList
                .getAvailableCommands(combinedPlayHeuristic, BattleAiMod.actionHeuristics);
    }

    public boolean isDone() {
        return isDone;
    }

    public int getPlayerHealth() {
        return saveState.getPlayerHealth();
    }

    public static int getPlayerDamage(StateNode node) {
        return node.controller.startingHealth - node.saveState.getPlayerHealth();
    }

    /**
     * This is used for end of battle score, only health matters here
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
                getPlayerDamage(node) * -1 +
                TurnNode.getPotionScore(node.saveState) +
                TurnNode.getRelicScore(node.saveState) +
                lessonLearnedScore +
                additonalHeuristicScore;
    }
}
