package fitnessevolution;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * In-process integration tests for the poll-mode daemon. Spins up a
 * {@link PollLoop} in a background thread, drives it through the handshake
 * by writing to {@code ipc/ModOutput.txt}, and verifies the daemon's
 * outputs and wiping behavior.
 */
class PollLoopTest {

    private static final String TEMPLATE_EXPR =
        "SUM_DAMAGE_DEALT - 2.0*DAMAGE_RECEIVED - MONSTERS_REMAINING "
            + "- 0.1*SUM_MONSTER_HEALTH + POWERS_PLAYED";

    private static final long POLL_MS = 20L;        // fast polling for tests
    private static final long TIMEOUT_MS = 5_000L;  // generous to avoid flakes

    @AfterEach
    void restoreConfig() {
        Config.configure(Path.of("."));
    }

    @Test
    void singleWarmupCycle(@TempDir Path tmp) throws Exception {
        seedWorkdir(tmp);
        Config.configure(tmp);

        PollLoop loop = new PollLoop("tw", 42L, POLL_MS);
        DaemonHarness harness = DaemonHarness.start(loop);
        try {
            // Mod writes 1 template + READY
            writeModOutputWithReady(tmp,
                "FITNESS=0\n" + TEMPLATE_EXPR + "\n");
            waitUntilJeneticsReady(tmp);

            List<String> outLines = Files.readAllLines(tmp.resolve("ipc/JeneticsOutput.txt"));
            // Strip blanks, expect 20 expressions + 1 READY = 21
            List<String> nonBlank = stripBlank(outLines);
            assertEquals(Config.POPULATION_SIZE + 1, nonBlank.size());
            assertEquals(JeneticsIO.READY_TOKEN, nonBlank.get(nonBlank.size() - 1));
            // First line should still be our template (canonicalised)
            assertTrue(nonBlank.get(0).contains("SUM_DAMAGE_DEALT"));

            // Daemon must have wiped ModOutput after producing its output
            waitUntilModOutputEmpty(tmp);

            // Step counter advanced to 1
            assertEquals(1, readStep(tmp, "tw"));
        } finally {
            harness.shutdownAndJoin();
        }
    }

    @Test
    void twoIterationsWarmupThenEvolve(@TempDir Path tmp) throws Exception {
        seedWorkdir(tmp);
        Config.configure(tmp);

        PollLoop loop = new PollLoop("te", 42L, POLL_MS);
        DaemonHarness harness = DaemonHarness.start(loop);
        try {
            // --- Iteration 1: warmup ---
            writeModOutputWithReady(tmp, "FITNESS=0\n" + TEMPLATE_EXPR + "\n");
            waitUntilJeneticsReady(tmp);
            List<String> gen0 = readExpressionsOnly(tmp);
            assertEquals(Config.POPULATION_SIZE, gen0.size());

            // Simulate mod consuming JeneticsOutput
            JeneticsIO.truncateToEmpty(tmp.resolve("ipc/JeneticsOutput.txt"));
            waitUntilModOutputEmpty(tmp);

            // --- Iteration 2: evolve ---
            writeModOutputWithReady(tmp, fullModPayload(gen0));
            waitUntilJeneticsReady(tmp);
            List<String> gen1 = readExpressionsOnly(tmp);
            assertEquals(Config.POPULATION_SIZE, gen1.size());

            assertEquals(2, readStep(tmp, "te"));
        } finally {
            harness.shutdownAndJoin();
        }
    }

    @Test
    void malformedInputIsWipedAndLoopContinues(@TempDir Path tmp) throws Exception {
        seedWorkdir(tmp);
        Config.configure(tmp);

        PollLoop loop = new PollLoop("tm", 42L, POLL_MS);
        DaemonHarness harness = DaemonHarness.start(loop);
        try {
            // Send a non-finite fitness — JeneticsIO rejects, PollLoop must wipe.
            writeModOutputWithReady(tmp,
                "FITNESS=NaN\n" + TEMPLATE_EXPR + "\n");
            waitUntilModOutputEmpty(tmp);

            // Step counter did NOT advance for malformed input.
            assertEquals(0, readStep(tmp, "tm"));

            // Now send a valid one — daemon should still be alive and process it.
            writeModOutputWithReady(tmp, "FITNESS=0\n" + TEMPLATE_EXPR + "\n");
            waitUntilJeneticsReady(tmp);
            assertEquals(1, readStep(tmp, "tm"));
        } finally {
            harness.shutdownAndJoin();
        }
    }

    @Test
    void requestShutdownReturnsPromptly(@TempDir Path tmp) throws Exception {
        seedWorkdir(tmp);
        Config.configure(tmp);
        PollLoop loop = new PollLoop("ts", 42L, POLL_MS);
        DaemonHarness harness = DaemonHarness.start(loop);

        // Let the daemon enter its polling loop.
        Thread.sleep(100);
        long t0 = System.currentTimeMillis();
        harness.shutdownAndJoin();
        long elapsed = System.currentTimeMillis() - t0;
        assertTrue(elapsed < 2_000,
            "daemon should exit quickly after requestShutdown; took " + elapsed + "ms");
    }

    @Test
    void jeneticsOutputHasReadyOnLastLine(@TempDir Path tmp) throws Exception {
        seedWorkdir(tmp);
        Config.configure(tmp);
        PollLoop loop = new PollLoop("tr", 42L, POLL_MS);
        DaemonHarness harness = DaemonHarness.start(loop);
        try {
            writeModOutputWithReady(tmp, "FITNESS=0\n" + TEMPLATE_EXPR + "\n");
            waitUntilJeneticsReady(tmp);

            Path jen = tmp.resolve("ipc/JeneticsOutput.txt");
            assertTrue(JeneticsIO.isPollReady(jen));
        } finally {
            harness.shutdownAndJoin();
        }
    }

    // ---- harness + helpers ------------------------------------------------

    /** Runs the daemon in a background thread; supports test teardown. */
    private static final class DaemonHarness {
        final PollLoop loop;
        final Thread thread;
        final AtomicReference<Throwable> failure = new AtomicReference<>();

        private DaemonHarness(PollLoop loop, Thread thread) {
            this.loop = loop;
            this.thread = thread;
        }

        static DaemonHarness start(PollLoop loop) {
            // Closure captures h.failure directly so any throw inside
            // loop.run() is observable after the thread terminates.
            DaemonHarness[] holder = new DaemonHarness[1];
            Thread t = new Thread(() -> {
                try {
                    loop.run();
                } catch (Throwable th) {
                    holder[0].failure.set(th);
                }
            }, "polltest-daemon");
            t.setDaemon(true);
            DaemonHarness h = new DaemonHarness(loop, t);
            holder[0] = h;
            t.start();
            return h;
        }

        void shutdownAndJoin() throws InterruptedException {
            loop.requestShutdown();
            thread.join(3_000);
            if (thread.isAlive()) {
                fail("daemon thread did not terminate within 3s");
            }
            Throwable t = failure.get();
            if (t != null) {
                fail("daemon thread threw: " + t);
            }
        }
    }

    private static void seedWorkdir(Path tmp) throws IOException {
        Path ipc = tmp.resolve("ipc");
        Files.createDirectories(ipc);
        Files.copy(Path.of("ipc/FeatureBank.txt"), ipc.resolve("FeatureBank.txt"));
    }

    private static void writeModOutputWithReady(Path tmp, String body) throws IOException {
        String content = body.endsWith("\n") ? body : body + "\n";
        Files.writeString(tmp.resolve("ipc/ModOutput.txt"), content + "READY\n");
    }

    private static String fullModPayload(List<String> exprs) {
        Random rng = new Random(1);
        StringBuilder sb = new StringBuilder();
        for (String e : exprs) {
            sb.append("FITNESS=").append(rng.nextDouble() * 10 - 5).append('\n')
              .append(e).append('\n');
        }
        return sb.toString();
    }

    private static void waitUntilJeneticsReady(Path tmp) throws Exception {
        Path p = tmp.resolve("ipc/JeneticsOutput.txt");
        pollUntilTrue(() -> {
            try { return JeneticsIO.isPollReady(p); }
            catch (IOException e) { return false; }
        }, "JeneticsOutput.txt to have READY");
    }

    private static void waitUntilModOutputEmpty(Path tmp) throws Exception {
        Path p = tmp.resolve("ipc/ModOutput.txt");
        pollUntilTrue(() -> {
            try { return Files.readString(p).trim().isEmpty(); }
            catch (IOException e) { return false; }
        }, "ModOutput.txt to be wiped");
    }

    private static void pollUntilTrue(BoolSupplier s, String desc) throws Exception {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (s.getAsBoolean()) return;
            Thread.sleep(15);
        }
        fail("timed out waiting for " + desc);
    }

    @FunctionalInterface
    private interface BoolSupplier { boolean getAsBoolean(); }

    private static List<String> stripBlank(List<String> lines) {
        List<String> out = new ArrayList<>(lines.size());
        for (String l : lines) {
            String t = l.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    private static List<String> readExpressionsOnly(Path tmp) throws IOException {
        List<String> all = stripBlank(
            Files.readAllLines(tmp.resolve("ipc/JeneticsOutput.txt")));
        // Drop trailing READY line
        if (!all.isEmpty() && all.get(all.size() - 1).equalsIgnoreCase(JeneticsIO.READY_TOKEN)) {
            return all.subList(0, all.size() - 1);
        }
        return all;
    }

    private static int readStep(Path tmp, String runId) throws IOException {
        Path f = tmp.resolve("runs/" + runId + ".step");
        if (!Files.exists(f)) return 0;
        return Integer.parseInt(Files.readString(f).trim());
    }
}
