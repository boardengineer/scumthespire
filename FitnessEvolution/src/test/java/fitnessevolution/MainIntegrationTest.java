package fitnessevolution;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests of the generation-agnostic {@link Main} CLI, run
 * in-process against a tempdir-sandboxed Config.
 */
class MainIntegrationTest {

    private static final String TEMPLATE_EXPR =
        "SUM_DAMAGE_DEALT - 2.0*DAMAGE_RECEIVED - MONSTERS_REMAINING "
            + "- 0.1*SUM_MONSTER_HEALTH + POWERS_PLAYED";

    @AfterEach
    void restoreConfig() {
        Config.configure(Path.of("."));
    }

    @Test
    void warmupFillsToPopulationSize(@TempDir Path tmp) throws IOException {
        seedWorkdir(tmp);
        Config.configure(tmp);

        // Mod seeds ModOutput with just the template (K=1 < POP=20).
        Files.writeString(
            tmp.resolve("ipc/ModOutput.txt"),
            "FITNESS=0\n" + TEMPLATE_EXPR + "\n");

        Main.main(new String[]{"--run-id=test"});

        Path out = tmp.resolve("ipc/JeneticsOutput.txt");
        assertTrue(Files.exists(out));
        List<String> lines = Files.readAllLines(out);
        assertEquals(Config.POPULATION_SIZE, lines.size(),
            "warmup should pad to POPULATION_SIZE");
        // First line should preserve (canonicalised) template.
        assertTrue(lines.get(0).contains("SUM_DAMAGE_DEALT"),
            "first output line should carry the template we seeded");

        // Step counter advanced from 0 → 1
        Path stepFile = tmp.resolve("runs/test.step");
        assertTrue(Files.exists(stepFile));
        assertEquals(1, Integer.parseInt(Files.readString(stepFile).trim()));
    }

    @Test
    void evolveStepWhenKEqualsPop(@TempDir Path tmp) throws IOException {
        seedWorkdir(tmp);
        Config.configure(tmp);

        // First call: warmup to produce a full population in JeneticsOutput.
        Files.writeString(
            tmp.resolve("ipc/ModOutput.txt"),
            "FITNESS=0\n" + TEMPLATE_EXPR + "\n");
        Main.main(new String[]{"--run-id=evo"});

        List<String> pop0 = Files.readAllLines(tmp.resolve("ipc/JeneticsOutput.txt"));
        assertEquals(Config.POPULATION_SIZE, pop0.size());

        // Mod writes back fitness for all 20 → we evolve.
        writeFullModOutput(tmp, pop0);
        Main.main(new String[]{"--run-id=evo"});

        List<String> pop1 = Files.readAllLines(tmp.resolve("ipc/JeneticsOutput.txt"));
        assertEquals(Config.POPULATION_SIZE, pop1.size());
        assertEquals(2, Integer.parseInt(
            Files.readString(tmp.resolve("runs/evo.step")).trim()));
    }

    @Test
    void endSentinelStopsWithoutAdvancingStep(@TempDir Path tmp) throws IOException {
        seedWorkdir(tmp);
        Config.configure(tmp);
        // Prime with one warmup step so the counter is at 1.
        Files.writeString(
            tmp.resolve("ipc/ModOutput.txt"),
            "FITNESS=0\n" + TEMPLATE_EXPR + "\n");
        Main.main(new String[]{"--run-id=stop"});
        assertEquals(1, Integer.parseInt(
            Files.readString(tmp.resolve("runs/stop.step")).trim()));

        Files.writeString(tmp.resolve("ipc/ModOutput.txt"), "END\n");
        Main.main(new String[]{"--run-id=stop"});

        // Step counter unchanged by END
        assertEquals(1, Integer.parseInt(
            Files.readString(tmp.resolve("runs/stop.step")).trim()));
    }

    @Test
    void truncatesWhenKExceedsPopulationSize(@TempDir Path tmp) throws IOException {
        seedWorkdir(tmp);
        Config.configure(tmp);

        // 25 identical expressions with varying fitness
        StringBuilder mod = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            mod.append("FITNESS=").append(i).append('\n')
               .append(TEMPLATE_EXPR).append('\n');
        }
        Files.writeString(tmp.resolve("ipc/ModOutput.txt"), mod.toString());

        Main.main(new String[]{"--run-id=big"});

        // Truncated → evolve path ran → POPULATION_SIZE output lines
        List<String> out = Files.readAllLines(tmp.resolve("ipc/JeneticsOutput.txt"));
        assertEquals(Config.POPULATION_SIZE, out.size());
    }

    @Test
    void separateRunIdsMaintainSeparateCounters(@TempDir Path tmp) throws IOException {
        seedWorkdir(tmp);
        Config.configure(tmp);

        Files.writeString(
            tmp.resolve("ipc/ModOutput.txt"),
            "FITNESS=0\n" + TEMPLATE_EXPR + "\n");
        Main.main(new String[]{"--run-id=runA"});
        Main.main(new String[]{"--run-id=runA"});

        Files.writeString(
            tmp.resolve("ipc/ModOutput.txt"),
            "FITNESS=0\n" + TEMPLATE_EXPR + "\n");
        Main.main(new String[]{"--run-id=runB"});

        assertEquals(2, Integer.parseInt(
            Files.readString(tmp.resolve("runs/runA.step")).trim()));
        assertEquals(1, Integer.parseInt(
            Files.readString(tmp.resolve("runs/runB.step")).trim()));
    }

    // ---- helpers ------------------------------------------------------------

    private static void seedWorkdir(Path tmp) throws IOException {
        Path ipc = tmp.resolve("ipc");
        Files.createDirectories(ipc);
        Files.copy(Path.of("ipc/FeatureBank.txt"), ipc.resolve("FeatureBank.txt"));
    }

    private static void writeFullModOutput(Path tmp, List<String> exprs) throws IOException {
        Random rng = new Random(1);
        StringBuilder mod = new StringBuilder();
        for (String e : exprs) {
            mod.append("FITNESS=").append(rng.nextDouble() * 10 - 5).append('\n')
               .append(e).append('\n');
        }
        Files.writeString(tmp.resolve("ipc/ModOutput.txt"), mod.toString());
    }
}
