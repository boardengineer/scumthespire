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
        while ((!isComplete || bestPathRunner.hasNext()) && !foundCommand) {
            if (!bestPathRunner.hasNext()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                continue;
            }
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
                System.err.println("Rerun Controller reporting done");
                isDone = true;
            }
        }
    }

    public boolean isDone() {
        return isDone;
    }
}
