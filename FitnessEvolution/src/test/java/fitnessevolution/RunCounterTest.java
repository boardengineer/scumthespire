package fitnessevolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RunCounterTest {

    @Test
    void missingFileReturnsZero(@TempDir Path tmp) throws IOException {
        Path f = tmp.resolve("nonexistent.step");
        assertEquals(0, RunCounter.load(f));
    }

    @Test
    void loadAndIncrementReturnsPreviousValue(@TempDir Path tmp) throws IOException {
        Path f = tmp.resolve("counter.step");
        assertEquals(0, RunCounter.loadAndIncrement(f));
        assertEquals(1, RunCounter.loadAndIncrement(f));
        assertEquals(2, RunCounter.loadAndIncrement(f));
        assertEquals(3, RunCounter.load(f));
    }

    @Test
    void createsParentDirectory(@TempDir Path tmp) throws IOException {
        Path f = tmp.resolve("deep/nested/dir/x.step");
        RunCounter.loadAndIncrement(f);
        assertTrue(Files.exists(f));
    }

    @Test
    void rejectsNonIntegerContents(@TempDir Path tmp) throws IOException {
        Path f = tmp.resolve("bad.step");
        Files.writeString(f, "not a number\n");
        assertThrows(IOException.class, () -> RunCounter.load(f));
    }

    @Test
    void tolerantOfTrailingWhitespace(@TempDir Path tmp) throws IOException {
        Path f = tmp.resolve("ws.step");
        Files.writeString(f, "  7  \n");
        assertEquals(7, RunCounter.load(f));
    }
}
