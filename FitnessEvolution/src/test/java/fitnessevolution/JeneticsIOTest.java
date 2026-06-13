package fitnessevolution;

import io.jenetics.Genotype;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class JeneticsIOTest {

    private ISeq<Var<Double>> vars;
    private ISeq<Op<Double>> terminals;
    private TreeNode<Op<Double>> template;

    private static final String TEMPLATE_EXPR =
        "SUM_DAMAGE_DEALT - 2.0*DAMAGE_RECEIVED - MONSTERS_REMAINING "
            + "- 0.1*SUM_MONSTER_HEALTH + POWERS_PLAYED";

    @BeforeEach
    void setUp() throws IOException {
        RandomRegistry.random(new Random(11));
        vars = FeatureBankLoader.loadVars(Path.of("ipc/FeatureBank.txt"));
        terminals = OpSet.terminals(vars);
        template = MathExprIO.parseExpression(TEMPLATE_EXPR);
    }

    @Test
    void writePopulationRoundTripsAsExpressions(@TempDir Path tmp) throws IOException {
        GPEngine engine = new GPEngine(OpSet.OPS, terminals, 7, 20, 1L);
        List<Genotype<ProgramGene<Double>>> pop = engine.initialPopulation(template, vars);

        Path out = tmp.resolve("JeneticsOutput.txt");
        List<String> canonical = JeneticsIO.writePopulation(pop, out);

        List<String> lines = Files.readAllLines(out);
        assertEquals(pop.size(), lines.size());
        assertEquals(canonical, lines);

        // Rehydrate → same canonical forms
        List<Genotype<ProgramGene<Double>>> rehydrated =
            engine.rehydratePopulation(canonical, vars);
        for (int i = 0; i < pop.size(); i++) {
            String reSer = MathExprIO.serialize(rehydrated.get(i).gene().toTreeNode());
            assertEquals(canonical.get(i), reSer,
                "expression #" + i + " should rehydrate to identical canonical form");
        }
    }

    @Test
    void readModOutputParsesFitnessExprPairs(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("ModOutput.txt");
        Files.writeString(file,
            "FITNESS=12.5\n"
                + "POWERS_PLAYED\n"
                + "FITNESS=-3.0\n"
                + "DAMAGE_RECEIVED + 1.0\n");
        List<JeneticsIO.ModResult> r = JeneticsIO.readModOutput(file);
        assertNotNull(r);
        assertEquals(2, r.size());
        assertEquals(12.5, r.get(0).fitness(), 0);
        assertEquals("POWERS_PLAYED", r.get(0).expression());
        assertEquals(-3.0, r.get(1).fitness(), 0);
    }

    @Test
    void readModOutputHonoursEndSentinel(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("ModOutput.txt");
        Files.writeString(file, "END\n");
        assertNull(JeneticsIO.readModOutput(file),
            "END on first non-blank line should produce null sentinel");
    }

    @Test
    void readModOutputRejectsOddLineCount(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("ModOutput.txt");
        Files.writeString(file, "FITNESS=1.0\nA\nFITNESS=2.0\n");
        assertThrows(IllegalStateException.class,
            () -> JeneticsIO.readModOutput(file));
    }

    @Test
    void readModOutputRejectsMissingFitnessPrefix(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("ModOutput.txt");
        Files.writeString(file, "1.0\nA\n");
        assertThrows(IllegalStateException.class,
            () -> JeneticsIO.readModOutput(file));
    }

    @Test
    void readModOutputRejectsBadDouble(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("ModOutput.txt");
        Files.writeString(file, "FITNESS=not_a_number\nA\n");
        assertThrows(IllegalStateException.class,
            () -> JeneticsIO.readModOutput(file));
    }

    @Test
    void readModOutputRejectsEmpty(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("ModOutput.txt");
        Files.writeString(file, "");
        assertThrows(IllegalStateException.class,
            () -> JeneticsIO.readModOutput(file));
    }
}
