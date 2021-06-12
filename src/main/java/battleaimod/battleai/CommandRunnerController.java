package battleaimod.battleai;

import battleaimod.BattleAiMod;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import ludicrousspeed.Controller;
import ludicrousspeed.simulator.commands.Command;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class CommandRunnerController implements Controller {
    public boolean isDone = false;

    public List<Command> bestPath;
    private List<Command> queuedPath;

    public Iterator<Command> bestPathRunner;

    boolean isComplete;
    boolean wouldComplete = true;

    public HashMap<String, Long> runTimes;

    public CommandRunnerController(List<Command> commands, boolean isComplete) {
        runTimes = new HashMap<>();
        this.isComplete = isComplete;
        bestPath = commands;
        bestPathRunner = commands.iterator();
    }

    public void updateBestPath(List<Command> commands, boolean wouldComplete) {
        queuedPath = commands;
        if (!bestPathRunner.hasNext()) {
            Iterator<Command> oldPath = bestPath.iterator();
            Iterator<Command> newPath = commands.iterator();

            while (oldPath.hasNext()) {
                oldPath.next();
                newPath.next();
            }

            bestPathRunner = newPath;
            this.isComplete = wouldComplete;
            bestPath = queuedPath;
        }

        this.wouldComplete = wouldComplete;
    }

    public void step() {
        if (isDone) {
            return;
        }
        boolean foundCommand = false;
        while (bestPathRunner.hasNext() && !foundCommand) {
            Command command = bestPathRunner.next();
            if (command != null) {
                foundCommand = true;
                command.execute();
            } else {
                foundCommand = true;
            }
        }
        if (!BattleAiMod.isServer) {
            AbstractDungeon.player.hand.refreshHandLayout();
        }

        if (!bestPathRunner.hasNext()) {
            if (isComplete) {
                isDone = true;
            } else if (queuedPath != null && queuedPath.size() > bestPath.size()) {
                System.err.println("Enqueueing path...");
                Iterator<Command> oldPath = bestPath.iterator();
                Iterator<Command> newPath = queuedPath.iterator();

                while (oldPath.hasNext()) {
                    oldPath.next();
                    newPath.next();
                }

                bestPathRunner = newPath;
                this.isComplete = wouldComplete;
                bestPath = queuedPath;
            }
        }
    }

    public boolean isDone() {
        return isDone;
    }
}
