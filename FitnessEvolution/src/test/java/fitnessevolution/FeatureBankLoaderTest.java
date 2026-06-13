package fitnessevolution;

import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FeatureBankLoaderTest {

    @Test
    void loadsRealFeatureBank() throws IOException {
        Path bank = Path.of("ipc/FeatureBank.txt");
        List<String> names = FeatureBankLoader.loadNames(bank);
        assertEquals(List.of(
            "SUM_DAMAGE_DEALT",
            "DAMAGE_RECEIVED",
            "MONSTERS_REMAINING",
            "SUM_MONSTER_HEALTH",
            "POWERS_PLAYED"
        ), names);
    }

    @Test
    void skipsBlanksAndComments(@org.junit.jupiter.api.io.TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("bank.txt");
        Files.writeString(file, """
            # leading comment
            FEATURE_A

            FEATURE_B
                # indented comment with whitespace
            FEATURE_C
            """);
        assertEquals(List.of("FEATURE_A", "FEATURE_B", "FEATURE_C"),
            FeatureBankLoader.loadNames(file));
    }

    @Test
    void varsAreIndexedInOrder(@org.junit.jupiter.api.io.TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("bank.txt");
        Files.writeString(file, "A\nB\nC\n");
        ISeq<Var<Double>> vars = FeatureBankLoader.loadVars(file);
        assertEquals(3, vars.size());
        assertEquals("A", vars.get(0).name());
        assertEquals("B", vars.get(1).name());
        assertEquals("C", vars.get(2).name());
        assertEquals(0, vars.get(0).index());
        assertEquals(1, vars.get(1).index());
        assertEquals(2, vars.get(2).index());
    }
}
