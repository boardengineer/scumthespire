package fitnessevolution;

import io.jenetics.ext.util.Tree;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.op.Const;
import io.jenetics.prog.op.MathExpr;
import io.jenetics.prog.op.MathOp;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Var;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MathExprIO {
    private MathExprIO() {}

    public static TreeNode<Op<Double>> parseTemplate(Path path) throws IOException {
        return parseExpression(Files.readString(path).trim());
    }

    /** Parse a MathExpr string into a mutable {@link TreeNode}. */
    public static TreeNode<Op<Double>> parseExpression(String expression) {
        return TreeNode.ofTree(MathExpr.parse(expression).tree());
    }

    /**
     * Canonical MathExpr string for a tree. Before handing the tree to
     * {@link MathExpr#toString} we:
     * <ol>
     *   <li>Replace every non-Var leaf (e.g. {@code EphemeralConst}) with a
     *       plain {@link Const} so numeric literals are emitted instead of
     *       {@code C(-0.61…)} function-call syntax.</li>
     *   <li>Rewrite every {@code neg(x)} subtree as {@code (0 - x)} so the
     *       output sticks to the locked operator set {@code +, -, *, /}.
     *       {@link MathExpr#parse} introduces {@code neg} nodes when it
     *       reads a unary minus, so we normalise those away.</li>
     * </ol>
     */
    public static String serialize(Tree<? extends Op<Double>, ?> tree) {
        TreeNode<Op<Double>> canonical = foldZeroSubConst(
            rewriteNeg(canonicalizeConstants(TreeNode.ofTree(tree))));
        return new MathExpr(canonical).toString();
    }

    @SuppressWarnings("unchecked")
    private static TreeNode<Op<Double>> canonicalizeConstants(TreeNode<Op<Double>> tree) {
        return tree.map(op -> {
            if (op instanceof Var<?>) {
                return op;
            }
            if (op.arity() == 0) {
                Double value = ((Op<Double>) op).apply(new Double[0]);
                return Const.of(value);
            }
            return op;
        });
    }

    private static TreeNode<Op<Double>> rewriteNeg(TreeNode<Op<Double>> node) {
        if (isNeg(node.value()) && node.childCount() == 1) {
            TreeNode<Op<Double>> sub = TreeNode.of(MathOp.SUB);
            sub.attach(TreeNode.of(Const.of(0.0)));
            sub.attach(rewriteNeg(node.childAt(0)));
            return sub;
        }
        TreeNode<Op<Double>> copy = TreeNode.of(node.value());
        for (int i = 0; i < node.childCount(); i++) {
            copy.attach(rewriteNeg(node.childAt(i)));
        }
        return copy;
    }

    private static boolean isNeg(Op<Double> op) {
        return op == MathOp.NEG || "neg".equals(op.name());
    }

    /**
     * After {@link #rewriteNeg}, any originally-negative constant becomes
     * {@code SUB(Const(0), Const(|v|))}. Fold that back to {@code Const(-|v|)}
     * so the serialized form matches a gen-0 tree that had the negative
     * constant baked in — keeps the canonical form stable across rehydration.
     */
    @SuppressWarnings("unchecked")
    private static TreeNode<Op<Double>> foldZeroSubConst(TreeNode<Op<Double>> node) {
        TreeNode<Op<Double>> rebuilt = TreeNode.of(node.value());
        for (int i = 0; i < node.childCount(); i++) {
            rebuilt.attach(foldZeroSubConst(node.childAt(i)));
        }
        if (rebuilt.value() == MathOp.SUB && rebuilt.childCount() == 2) {
            Op<Double> left = rebuilt.childAt(0).value();
            Op<Double> right = rebuilt.childAt(1).value();
            if (left instanceof Const<?> && right instanceof Const<?>) {
                Double lv = ((Const<Double>) left).value();
                Double rv = ((Const<Double>) right).value();
                if (lv != null && lv == 0.0 && rv != null) {
                    return TreeNode.of(Const.of(-rv));
                }
            }
        }
        return rebuilt;
    }
}
