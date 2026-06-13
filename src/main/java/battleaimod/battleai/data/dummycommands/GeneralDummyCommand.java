package battleaimod.battleai.data.dummycommands;

import ludicrousspeed.simulator.commands.Command;

public class GeneralDummyCommand implements DummyCommand {
    private final Command cmd;
    public GeneralDummyCommand(Command cmd){
        this.cmd = cmd;
    }

    @Override
    public Command getRealCommand() {
        return cmd;
    }
}
