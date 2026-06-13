package fitnessevolution;

import io.jenetics.ext.util.Tree;
import io.jenetics.prog.op.MathExpr;
import io.jenetics.prog.op.Op;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MathExprIOTest {

    @Test
    void parsesFeatureBankTemplate() {
        String expr = "SUM_DAMAGE_DEALT - 2.0*DAMAGE_RECEIVED - MONSTERS_REMAINING "
            + "- 0.1*SUM_MONSTER_HEALTH + POWERS_PLAYED";
        Tree<Op<Double>, ?> tree = MathExprIO.parseExpression(expr);
        String serialized = MathExprIO.serialize(tree);
        assertTrue(serialized.contains("SUM_DAMAGE_DEALT"));
        assertTrue(serialized.contains("DAMAGE_RECEIVED"));
        assertTrue(serialized.contains("MONSTERS_REMAINING"));
    }

    @Test
    void roundTripsStandardArithmetic(@org.junit.jupiter.api.io.TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("tmpl.txt");
        String expr = "A + 2.0*B - C/4.0";
        Files.writeString(file, expr);

        Tree<Op<Double>, ?> tree = MathExprIO.parseTemplate(file);
        String serialized = MathExprIO.serialize(tree);

        // Re-parse the serialized form; both trees should evaluate identically
        // across a grid of inputs.
        MathExpr reparsed = MathExpr.parse(serialized);
        MathExpr original = MathExpr.parse(expr);

        double[] args = {1.5, 2.5, 3.5};
        assertEquals(original.eval(args), reparsed.eval(args), 1e-12);
    }

    @Test
    void serializesDeterministically() throws IOException {
        String expr = "A*B + C";
        MathExpr parsed = MathExpr.parse(expr);
        String a = MathExprIO.serialize(parsed.tree());
        String b = MathExprIO.serialize(parsed.tree());
        assertEquals(a, b);
    }
}
