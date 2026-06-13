package fitnessevolution;

import java.nio.file.Path;

/**
 * Central location for filesystem paths and run-level tunables.
 * GP algorithm constants (tournament k, crossover prob, etc.) live in
 * {@link GPEngine} because they are algorithm-internal.
 *
 * <p>Paths are derived from a mutable {@link #baseDir} (default: current
 * working directory) so integration tests can point the whole program at a
 * temporary directory by calling {@link #configure(Path)}.
 */
public final class Config {

    private static Path baseDir = Path.of(".");

    public static final int POPULATION_SIZE = 15;
    public static final int MAX_DEPTH = 7;
    public static final long DEFAULT_SEED = 42L;
    public static final String DEFAULT_RUN_ID = "default";

    /** First non-whitespace line of ModOutput.txt = this → we exit cleanly. */
    public static final String END_SENTINEL = "END";

    private Config() {}

    /**
     * Point all subsequent path lookups at {@code newBaseDir}. Call this
     * once before invoking {@link Main#main} when driving the program
     * from tests or from a harness that wants to sandbox its working files.
     */
    public static void configure(Path newBaseDir) {
        baseDir = newBaseDir;
    }

    public static Path baseDir() {
        return baseDir;
    }

    public static Path ipcDir()            { return baseDir.resolve("ipc"); }
    public static Path runsDir()           { return baseDir.resolve("runs"); }
    public static Path logsDir()           { return baseDir.resolve("logs"); }
    public static Path featureBank()       { return ipcDir().resolve("FeatureBank.txt"); }
    public static Path jeneticsOutput()    { return ipcDir().resolve("JeneticsOutput.txt"); }
    public static Path modOutput()         { return ipcDir().resolve("ModOutput.txt"); }
    public static Path runStepFile(String runId) {
        return runsDir().resolve(runId + ".step");
    }
    public static Path evolutionLog()      { return logsDir().resolve("evolution_log.txt"); }
}
