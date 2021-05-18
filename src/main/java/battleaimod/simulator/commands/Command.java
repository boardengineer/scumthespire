package battleaimod.simulator.commands;

public interface Command {
    void execute();

    String encode();
}