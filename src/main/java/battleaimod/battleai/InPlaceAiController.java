package battleaimod.battleai;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import ludicrousspeed.Controller;
import ludicrousspeed.simulator.commands.CardCommand;
import ludicrousspeed.simulator.commands.Command;
import ludicrousspeed.simulator.commands.CommandList;

import java.util.List;

public class InPlaceAiController implements Controller {
    @Override
    public void step() {
        List<Command> commands = CommandList.getAvailableCommands();

        Command someCommand = null;

        for (Command command : commands) {
            if (command instanceof CardCommand) {
                command.execute();
                return;
            }

            someCommand = command;
        }

        if (someCommand != null) {
            someCommand.execute();
        }
    }

    @Override
    public boolean isDone() {
        return AbstractDungeon.getCurrRoom().monsters.areMonstersBasicallyDead();
    }
}
