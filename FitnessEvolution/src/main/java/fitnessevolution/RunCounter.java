package fitnessevolution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Per-run step counter persisted as a single-integer file.
 *
 * <p>Each invocation of {@link Main} advances the counter for its run id
 * by one. The counter drives the per-step RNG seed
 * ({@code effectiveSeed = baseSeed + step}) so runs are reproducible
 * without us storing any other state between calls.
 *
 * <p>File format: a single decimal integer, optionally followed by a
 * trailing newline. Missing file == step 0.
 */
public final class RunCounter {

    private RunCounter() {}

    /**
     * Read the current step for this run, then increment and persist.
     *
     * @return the step value <em>before</em> incrementing (i.e., the step
     *         this invocation should use)
     */
    public static int loadAndIncrement(Path stepFile) throws IOException {
        int current = load(stepFile);
        write(stepFile, current + 1);
        return current;
    }

    /** Return the current step, or 0 if the file does not exist. */
    public static int load(Path stepFile) throws IOException {
        if (!Files.exists(stepFile)) {
            return 0;
        }
        String raw = Files.readString(stepFile).trim();
        if (raw.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IOException("step file " + stepFile + " is not an integer: " + raw, e);
        }
    }

    private static void write(Path stepFile, int value) throws IOException {
        Path parent = stepFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(stepFile, Integer.toString(value));
    }
}
