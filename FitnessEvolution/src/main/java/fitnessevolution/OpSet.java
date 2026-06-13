package fitnessevolution;

import io.jenetics.prog.op.EphemeralConst;
import io.jenetics.prog.op.MathOp;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class OpSet {
    public static final double CONST_MIN = -5.0;
    public static final double CONST_MAX = 5.0;

    public static final ISeq<Op<Double>> OPS = ISeq.of(
        MathOp.ADD,
        MathOp.SUB,
        MathOp.MUL,
        MathOp.DIV
    );

    public static ISeq<Op<Double>> terminals(ISeq<Var<Double>> vars) {
        Op<Double> ephemeral = EphemeralConst.of(
            "C",
            () -> CONST_MIN + ThreadLocalRandom.current().nextDouble() * (CONST_MAX - CONST_MIN)
        );
        List<Op<Double>> combined = new ArrayList<>(vars.size() + 1);
        combined.add(ephemeral);
        combined.addAll(vars.asList());
        return ISeq.of(combined);
    }

    private OpSet() {}
}
