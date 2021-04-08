package battleaimod.battleai;

public interface Command {
    void execute();

    String encode();
}