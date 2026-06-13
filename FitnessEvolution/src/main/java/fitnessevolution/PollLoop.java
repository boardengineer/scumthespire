package fitnessevolution;

import io.jenetics.util.RandomRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Long-running daemon loop for poll-mode operation.
 *
 * <p>Each iteration:
 * <ol>
 *   <li>Poll {@code ipc/ModOutput.txt} every {@link #pollIntervalMs} ms until
 *       its last non-blank line is {@link JeneticsIO#READY_TOKEN READY}.</li>
 *   <li>Read + parse the file; dispatch to {@link GPProcessor#produce}.</li>
 *   <li>Atomically write N canonical expressions + a trailing
 *       {@code READY} line to {@code ipc/JeneticsOutput.txt}.</li>
 *   <li>Truncate {@code ipc/ModOutput.txt} to empty.</li>
 *   <li>Increment {@code runs/<run-id>.step} and log one line.</li>
 * </ol>
 *
 * <p>Shutdown: installs a JVM shutdown hook that sets a flag and interrupts
 * the main thread. On Ctrl-C / SIGTERM the current iteration finishes
 * before the loop exits — a second signal lets the JVM force-kill.
 *
 * <p>Error policy:
 * <ul>
 *   <li>Malformed ModOutput (parse error, non-finite fitness, unparseable
 *       expression, K=0) → log raw content, wipe ModOutput, keep polling.</li>
 *   <li>IO errors (read/write failure) → propagate; {@link Main} exits 3.</li>
 * </ul>
 */
public final class PollLoop {

    private static final Logger LOG = Logger.getLogger(PollLoop.class.getName());

    private final String runId;
    private final long baseSeed;
    private final long pollIntervalMs;

    private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    private volatile Thread mainThread;

    public PollLoop(String runId, long baseSeed, long pollIntervalMs) {
        if (pollIntervalMs <= 0) {
            throw new IllegalArgumentException(
                "poll interval must be > 0 ms, got " + pollIntervalMs);
        }
        this.runId = runId;
        this.baseSeed = baseSeed;
        this.pollIntervalMs = pollIntervalMs;
    }

    /** Programmatically request a graceful shutdown (used by tests). */
    public void requestShutdown() {
        shutdownRequested.set(true);
        Thread t = mainThread;
        if (t != null) {
            t.interrupt();
        }
    }

    public void run() throws IOException {
        mainThread = Thread.currentThread();
        Thread hook = new Thread(() -> {
            // Signal the main thread to stop …
            requestShutdown();
            // … then block until it's actually done (or 5 s elapse) so
            // the JVM doesn't halt mid-iteration.
            Thread t = mainThread;
            if (t != null && t != Thread.currentThread()) {
                try {
                    t.join(5_000);
                } catch (InterruptedException ignored) {
                    // Best effort; fall through and let JVM halt.
                }
            }
        }, "FE-poll-shutdown");
        boolean hookRegistered = registerShutdownHook(hook);
        System.out.println("poll mode started · run-id=" + runId
            + " · poll-interval=" + pollIntervalMs + "ms");
        appendLog("poll_start run=" + runId + " interval_ms=" + pollIntervalMs);

        try {
            ensureModOutputExists();
            loop();
        } finally {
            if (hookRegistered) {
                try {
                    Runtime.getRuntime().removeShutdownHook(hook);
                } catch (IllegalStateException ignored) {
                    // JVM already in shutdown; hook fires anyway, harmless.
                }
            }
            appendLog("poll_stop run=" + runId);
            System.out.println("poll mode stopped.");
        }
    }

    // ---- main loop ---------------------------------------------------------

    private void loop() throws IOException {
        Path modOut = Config.modOutput();
        while (!shutdownRequested.get()) {
            try {
                if (!JeneticsIO.isPollReady(modOut)) {
                    Thread.sleep(pollIntervalMs);
                    continue;
                }
                try {
                    processOneHandshake();
                } catch (IllegalArgumentException | IllegalStateException e) {
                    handleMalformedInput(e);
                }
            } catch (InterruptedException e) {
                // Only source of interrupt is the shutdown hook / requestShutdown.
                // Exit the loop regardless of how the flag looks.
                shutdownRequested.set(true);
                return;
            }
        }
    }

    // ---- single handshake --------------------------------------------------

    private void processOneHandshake() throws IOException {
        Path stepFile = Config.runStepFile(runId);
        int step = RunCounter.load(stepFile);
        long effectiveSeed = baseSeed + step;
        RandomRegistry.random(new Random(effectiveSeed));

        List<JeneticsIO.ModResult> results =
            JeneticsIO.readModOutput(Config.modOutput());
        if (results == null) {
            // Mod sent END; in poll mode we treat as unsupported input.
            throw new IllegalArgumentException(
                "END received in poll mode — use Ctrl-C to stop the daemon");
        }

        List<String> outputExprs = GPProcessor.produce(results, effectiveSeed);
        JeneticsIO.writeExpressionsAtomic(
            Config.jeneticsOutput(), outputExprs, /* appendReady */ true);
        // If truncateToEmpty throws (disk full / perm error), the daemon
        // exits 3 with JeneticsOutput already committed but ModOutput still
        // carrying its READY. On next startup we'll re-process the same
        // input with the same seed → identical JeneticsOutput. Mod may
        // redo 20 battles (wasted compute) but nothing is corrupted: same
        // seed → deterministic output. Step counter stays at its current
        // value because the increment below is skipped.
        JeneticsIO.truncateToEmpty(Config.modOutput());
        int nextStep = RunCounter.loadAndIncrement(stepFile);

        String msg = String.format(
            "run=%s step=%d input_k=%d output_n=%d seed=%d",
            runId, nextStep, results.size(), outputExprs.size(), effectiveSeed);
        appendLog(msg);
        System.out.println(msg);
    }

    // ---- error handling ----------------------------------------------------

    private void handleMalformedInput(Exception e) throws IOException {
        String preview = previewModOutput();
        String msg = "malformed ModOutput (wiped + continuing): "
            + e.getMessage() + " · content: " + preview;
        System.err.println("warning: " + msg);
        appendLog(msg);
        // Wipe regardless — prevents infinite loop on persistent bad READY.
        try {
            JeneticsIO.truncateToEmpty(Config.modOutput());
        } catch (IOException wipeErr) {
            // If we can't even wipe, escalate — this is a fatal IO condition.
            throw wipeErr;
        }
    }

    private String previewModOutput() {
        try {
            String content = Files.readString(Config.modOutput());
            String oneLine = content.replace('\n', '|').replace('\r', ' ');
            if (oneLine.length() > 200) {
                oneLine = oneLine.substring(0, 200) + "…";
            }
            return "'" + oneLine + "'";
        } catch (IOException e) {
            return "<unreadable: " + e.getMessage() + ">";
        }
    }

    // ---- lifecycle helpers -------------------------------------------------

    private void ensureModOutputExists() throws IOException {
        Path modOut = Config.modOutput();
        if (!Files.exists(modOut)) {
            Path parent = modOut.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(modOut, "");
        }
    }

    private static boolean registerShutdownHook(Thread hook) {
        try {
            Runtime.getRuntime().addShutdownHook(hook);
            return true;
        } catch (IllegalStateException e) {
            // JVM already shutting down — extremely unusual at startup.
            return false;
        }
    }

    private void appendLog(String message) throws IOException {
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
