package battleaimod.battleai;

import battleaimod.BattleAiMod;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import ludicrousspeed.simulator.commands.CardCommand;
import ludicrousspeed.simulator.commands.Command;
import ludicrousspeed.simulator.commands.CommandList;
import ludicrousspeed.simulator.commands.EndCommand;
import savestate.SaveState;

import java.util.Comparator;
import java.util.List;

import static battleaimod.ValueFunctions.getStateScore;

public class StateNode {
    private final BattleAiController controller;

    public SaveState saveState;
    private boolean initialized = false;
    private boolean isDone = false;

    // The list of lastCommands obtained by iterating up the parents will be the backwards sequence
    // of commands to get to this state.
    public final StateNode parent;
    public final Command lastCommand;

    // List of commands available from the current state, ordered by guessed result from best to
    // worst.
    public List<Command> commands;
    private int commandIndex = -1;

    // The number of actions taken in this turn, used to limit the total number of actions to
    // prevent infinite loops.
    int turnDepth;

    public StateNode(StateNode parent, Command lastCommand, BattleAiController controller) {
        this.parent = parent;
        this.lastCommand = lastCommand;
        this.controller = controller;
    }

    /**
     * Performs the next step and returns true iff the parent should load state
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

            if (AbstractDungeon.player.isDead || AbstractDungeon.player.isDying || saveState
                    .getPlayerHealth() < 1) {
                if (controller.deathNode == null ||
                        (controller.deathNode != null && controller.deathNode.saveState.turn < saveState.turn)) {
                    controller.deathNode = this;
                }

                isDone = true;
                return null;
            }

            if (isBattleOver()) {
                boolean isBestWin = controller.bestEnd == null ||
                        getStateScore(this) > getStateScore(controller.bestEnd);
                if (isBestWin) {
                    controller.bestEnd = this;
                }

                isDone = true;
                return null;
            } else {
                commandIndex = 0;
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
            if (card1.upgraded && !card2.upgraded) {
                return -1;
            }

            return 0;
        };

        Comparator<Command> commandComparator = (command1, command2) -> {
            if (command1 instanceof CardCommand && command2 instanceof CardCommand) {
                CardCommand cardCommand1 = (CardCommand) command1;
                CardCommand cardCommand2 = (CardCommand) command2;

                AbstractCard card1 = AbstractDungeon.player.hand.group.get(cardCommand1.cardIndex);
                AbstractCard card2 = AbstractDungeon.player.hand.group.get(cardCommand2.cardIndex);

                int cardHeuristic = combinedPlayHeuristic.compare(card1, card2);
                if (cardHeuristic != 0) {
                    return cardHeuristic;
                } else {
                    return cardCommand1.cardIndex - cardCommand2.cardIndex;
                }
            }

            return 0;
        };

        commands = CommandList.getAvailableCommands(null, BattleAiMod.actionHeuristics);

        commands.sort(commandComparator);
//
//        System.err.println("Commands" + commands);
//
    }

    public boolean isDone() {
        return isDone;
    }

    public static int getPlayerDamage(StateNode node) {
        return node.controller.startingHealth - node.saveState.getPlayerHealth();
    }
}
