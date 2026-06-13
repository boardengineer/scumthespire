package fitnessevolution;

import io.jenetics.util.RandomRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * CLI entry point.
 *
 * <p>Two modes:
 * <ul>
 *   <li><b>One-shot</b> (default): read ModOutput once, produce next
 *       population, write JeneticsOutput, increment step, exit. Mod drives
 *       the outer loop by calling the jar repeatedly.</li>
 *   <li><b>Poll</b> ({@code --poll}): long-running daemon. See
 *       {@link PollLoop}.</li>
 * </ul>
 *
 * <p>Flags:
 * <ul>
 *   <li>{@code --run-id=<id>} — isolates concurrent runs (default
 *       {@link Config#DEFAULT_RUN_ID}).</li>
 *   <li>{@code --seed=<long>} — base seed (default
 *       {@link Config#DEFAULT_SEED}). Effective per-step seed is
 *       {@code baseSeed + step}.</li>
 *   <li>{@code --poll} — enable poll mode.</li>
 *   <li>{@code --poll-interval-ms=<int>} — poll interval (default 100, only
 *       effective with {@code --poll}).</li>
 * </ul>
 *
 * <p>Exit codes: 0 ok, 2 user/config error, 3 IO error, 4 internal.
 */
public final class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private static final long DEFAULT_POLL_INTERVAL_MS = 100L;

    private Main() {}

    public static void main(String[] args) {
        try {
            CliArgs cli = parseArgs(args);

            // ----------------------------
            // SET ROOT DIRECTORY (NEW)
            // ----------------------------
            if (cli.rootDir != null) {
                Config.configure(cli.rootDir);
            }

            if (cli.poll) {
                new PollLoop(cli.runId, cli.baseSeed, cli.pollIntervalMs).run();
            } else {
                runOneShot(cli);
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("error: " + e.getMessage());
            System.exit(2);
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            System.exit(3);
        } catch (RuntimeException e) {
            System.err.println("internal error: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(4);
        }
    }

    // ---- one-shot path ----------------------------------------------------

    private static void runOneShot(CliArgs cli) throws IOException {
        Path stepFile = Config.runStepFile(cli.runId);
        int step = RunCounter.load(stepFile);
        long effectiveSeed = cli.baseSeed + step;
        RandomRegistry.random(new Random(effectiveSeed));

        if (!Files.exists(Config.modOutput())) {
            throw new IllegalArgumentException(
                Config.modOutput() + " does not exist — the mod must write "
                    + "it before invoking this jar");
        }
        List<JeneticsIO.ModResult> results =
            JeneticsIO.readModOutput(Config.modOutput());
        if (results == null) {
            appendLog("run=" + cli.runId + " step=" + step + " END received");
            System.out.println("END received — run stopped.");
            return;
        }

        List<String> outputExprs = GPProcessor.produce(results, effectiveSeed);
        Files.createDirectories(Config.jeneticsOutput().getParent());
        Files.writeString(
            Config.jeneticsOutput(),
            String.join("\n", outputExprs) + "\n");

        int nextStep = RunCounter.loadAndIncrement(stepFile);
        appendLog(String.format(
            "run=%s step=%d input_k=%d output_n=%d seed=%d",
            cli.runId, nextStep, results.size(), outputExprs.size(), effectiveSeed));

        System.out.println("run=" + cli.runId + " step=" + nextStep
            + " · wrote " + outputExprs.size() + " expressions to "
            + Config.jeneticsOutput());
    }

    // ---- arg parsing ------------------------------------------------------

    private record CliArgs(
        String runId, long baseSeed, boolean poll, long pollIntervalMs, Path rootDir
    ) {}

    private static CliArgs parseArgs(String[] args) {
        String runId = Config.DEFAULT_RUN_ID;
        long baseSeed = Config.DEFAULT_SEED;
        boolean poll = false;
        long pollIntervalMs = DEFAULT_POLL_INTERVAL_MS;
        Path rootDir = null;
        for (String a : args) {
            if (a.equals("--poll")) {
                poll = true;
            } else if (a.startsWith("--run-id=")) {
                runId = a.substring("--run-id=".length()).trim();
                if (runId.isEmpty()) {
                    throw new IllegalArgumentException("--run-id value cannot be empty");
                }
            } else if (a.startsWith("--seed=")) {
                try {
                    baseSeed = Long.parseLong(a.substring("--seed=".length()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("bad --seed value: " + a, e);
                }
            } else if (a.startsWith("--poll-interval-ms=")) {
                try {
                    pollIntervalMs = Long.parseLong(
                        a.substring("--poll-interval-ms=".length()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "bad --poll-interval-ms value: " + a, e);
                }
            } else if (a.startsWith("--root=")) {
                rootDir = Path.of(a.substring("--root=".length()));
            }
        }
        return new CliArgs(runId, baseSeed, poll, pollIntervalMs, rootDir);
    }

    private static void appendLog(String message) throws IOException {
        Path logFile = Config.evolutionLog();
        Path parent = logFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String line = Instant.now() + " " + message + "\n";
        Files.writeString(
            logFile,
            line,
            java.nio.file.StandardOpenOption.CREATE,
            java.nio.file.StandardOpenOption.APPEND);
        LOG.fine(message);
    }
}
