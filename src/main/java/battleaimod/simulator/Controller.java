package battleaimod.simulator;

import battleaimod.battleai.TurnNode;
import battleaimod.simulator.commands.Command;

import java.util.Iterator;
import java.util.List;

public interface Controller {
    /**
     * Will be called whenever the game is waiting for a user action.  a step call should execute
     * at most one command.
     */
    void step();

    /**
     * while isDone returns true, the action simulator will block and step.
     */
    boolean isDone();

    boolean runCommandMode();

    TurnNode committedTurn();

    int turnsLoaded();

    Iterator<Command> bestPathRunner();

    List<Command> bestPath();

    int maxTurnLoads();

    void updateBestPath(List<Command> commands, boolean wouldComplete);
}
