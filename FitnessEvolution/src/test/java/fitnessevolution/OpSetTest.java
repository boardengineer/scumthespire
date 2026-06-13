package fitnessevolution;

import io.jenetics.prog.op.MathOp;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpSetTest {

    @Test
    void opsContainsFourArithmetic() {
        assertEquals(4, OpSet.OPS.size());
        assertTrue(OpSet.OPS.asList().containsAll(List.of(
            MathOp.ADD, MathOp.SUB, MathOp.MUL, MathOp.DIV
        )));
    }

    @Test
    void terminalsContainsVarsPlusOneEphemeral() {
        ISeq<Var<Double>> vars = ISeq.of(Var.of("A", 0), Var.of("B", 1));
        ISeq<Op<Double>> terminals = OpSet.terminals(vars);
        assertEquals(vars.size() + 1, terminals.size());
        assertTrue(terminals.asList().containsAll(vars.asList()));
    }

    @Test
    void ephemeralConstStaysInRange() {
        ISeq<Op<Double>> terminals = OpSet.terminals(ISeq.empty());
        Op<Double> ephemeral = terminals.get(0);
        for (int i = 0; i < 1000; i++) {
            double v = ephemeral.apply(new Double[0]);
            assertTrue(v >= OpSet.CONST_MIN && v < OpSet.CONST_MAX,
                "constant " + v + " outside [" + OpSet.CONST_MIN + ", " + OpSet.CONST_MAX + ")");
        }
    }
}
