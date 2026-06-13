package fitnessevolution;

import io.jenetics.Genotype;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.MathExpr;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Dev-only harness: run the engine in-process against a synthetic fitness
 * function so we can see that evolution produces sensible populations without
 * needing the mod or file IPC wired up yet.
 *
 * Synthetic fitness: target value = 10.0 at fixed feature snapshot
 * {1, 1, 1, 1, 1}; fitness = -|eval - 10|; non-finite eval → -1000.
 */
public final class OfflineMockRun {

    private static final double[] FEATURES = {1.0, 1.0, 1.0, 1.0, 1.0};
    private static final double TARGET = 10.0;
    private static final double INVALID = -1000.0;
    private static final int GENERATIONS = 10;
    private static final int POPULATION = 20;
    private static final int MAX_DEPTH = 7;
    private static final long SEED = 42L;
    private static final int TOP_K_DISPLAY = 5;

    private static final String TEMPLATE_EXPR =
        "SUM_DAMAGE_DEALT - 2.0*DAMAGE_RECEIVED - MONSTERS_REMAINING "
            + "- 0.1*SUM_MONSTER_HEALTH + POWERS_PLAYED";

    public static void main(String[] args) throws Exception {
        RandomRegistry.random(new Random(SEED));

        Path featureBank = Path.of("ipc/FeatureBank.txt");

        ISeq<Var<Double>> vars = FeatureBankLoader.loadVars(featureBank);
        ISeq<Op<Double>> terminals = OpSet.terminals(vars);
        TreeNode<Op<Double>> templateTree = MathExprIO.parseExpression(TEMPLATE_EXPR);

        GPEngine engine = new GPEngine(
            OpSet.OPS, terminals, MAX_DEPTH, POPULATION, SEED);

        List<Genotype<ProgramGene<Double>>> pop =
            engine.initialPopulation(templateTree, vars);

        Scored bestEver = null;
        for (int gen = 0; gen < GENERATIONS; gen++) {
            List<Scored> scored = new ArrayList<>(pop.size());
            for (Genotype<ProgramGene<Double>> g : pop) {
                double f = mockFitness(g);
                scored.add(new Scored(g, f));
            }
            scored.sort(Comparator.comparingDouble(Scored::fitness).reversed());

            double meanFitness = scored.stream()
                .mapToDouble(Scored::fitness).average().orElse(Double.NaN);

            System.out.printf("=== Generation %2d | best %.3f | mean %.3f ===%n",
                gen, scored.get(0).fitness(), meanFitness);
            System.out.printf("%-6s %-12s %s%n", "rank", "fitness", "expression");
            for (int i = 0; i < Math.min(TOP_K_DISPLAY, scored.size()); i++) {
                Scored s = scored.get(i);
                System.out.printf("%-6d %-12.4f %s%n", i + 1, s.fitness(), s.expr());
            }
            System.out.println();

            if (bestEver == null || scored.get(0).fitness() > bestEver.fitness()) {
                bestEver = scored.get(0);
            }

            pop = engine.step(scored);
        }

        System.out.println("=== Best ever ===");
        System.out.printf("fitness %.6f%n%s%n", bestEver.fitness(), bestEver.expr());
    }

    private static double mockFitness(Genotype<ProgramGene<Double>> g) {
        try {
            MathExpr expr = new MathExpr(g.gene().toTreeNode());
            double v = expr.eval(FEATURES);
            if (!Double.isFinite(v)) return INVALID;
            return -Math.abs(v - TARGET);
        } catch (Exception e) {
            return INVALID;
        }
    }

    private OfflineMockRun() {}
}
