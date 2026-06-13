package fitnessevolution;

import io.jenetics.Genotype;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.MathExpr;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Core GP step logic, shared between {@link Main} (one-shot) and
 * {@link PollLoop} (daemon). Given K scored expressions from ModOutput,
 * produces {@code Config.POPULATION_SIZE} canonical expressions to write
 * to JeneticsOutput.
 *
 * <p>Decision table:
 * <ul>
 *   <li>K == 0 → {@link IllegalArgumentException} (caller should log + wipe + continue in poll mode, or exit in one-shot)</li>
 *   <li>K &lt; N → warmup: echo K canonicalised expressions + (N − K) fresh random trees. No evolution.</li>
 *   <li>K == N → one GP step via {@link GPEngine#step}.</li>
 *   <li>K &gt; N → truncate to top N by fitness (warn stderr), then evolve.</li>
 * </ul>
 */
public final class GPProcessor {

    private GPProcessor() {}

    /** Returns the N canonical expressions for the next JeneticsOutput write. */
    public static List<String> produce(
        List<JeneticsIO.ModResult> results, long effectiveSeed
    ) throws IOException {
        int k = results.size();
        int pop = Config.POPULATION_SIZE;
        if (k == 0) {
            throw new IllegalArgumentException(
                "ModOutput.txt has 0 scored expressions — cannot evolve from empty input");
        }

        ISeq<Var<Double>> vars = FeatureBankLoader.loadVars(Config.featureBank());
        ISeq<Op<Double>> terminals = OpSet.terminals(vars);
        GPEngine engine = new GPEngine(
            OpSet.OPS, terminals, Config.MAX_DEPTH, pop, effectiveSeed);

        if (k > pop) {
            System.err.println(
                "warning: ModOutput.txt has " + k + " > " + pop
                    + " entries; truncating to top " + pop + " by fitness");
            results = topNByFitness(results, pop);
            k = pop;
        }

        return (k < pop)
            ? warmupFill(results, engine)
            : evolveStep(results, engine, vars);
    }

    // ---- K < POP: warmup ---------------------------------------------------

    private static List<String> warmupFill(
        List<JeneticsIO.ModResult> results, GPEngine engine
    ) {
        int fillCount = Config.POPULATION_SIZE - results.size();
        List<String> expressions = new ArrayList<>(Config.POPULATION_SIZE);
        for (JeneticsIO.ModResult r : results) {
            expressions.add(canonicalise(r.expression()));
        }
        for (Genotype<ProgramGene<Double>> g : engine.randomTrees(fillCount)) {
            expressions.add(MathExprIO.serialize(g.gene().toTreeNode()));
        }
        return expressions;
    }

    // ---- K == POP: one GP step --------------------------------------------

    private static List<String> evolveStep(
        List<JeneticsIO.ModResult> results, GPEngine engine, ISeq<Var<Double>> vars
    ) {
        List<String> canonicalIn = new ArrayList<>(results.size());
        for (JeneticsIO.ModResult r : results) {
            canonicalIn.add(canonicalise(r.expression()));
        }
        List<Genotype<ProgramGene<Double>>> population =
            engine.rehydratePopulation(canonicalIn, vars);
        List<Scored> scored = new ArrayList<>(results.size());
        for (int i = 0; i < results.size(); i++) {
            scored.add(new Scored(population.get(i), results.get(i).fitness()));
        }
        List<Genotype<ProgramGene<Double>>> next = engine.step(scored);
        List<String> out = new ArrayList<>(next.size());
        for (Genotype<ProgramGene<Double>> g : next) {
            out.add(MathExprIO.serialize(g.gene().toTreeNode()));
        }
        return out;
    }

    // ---- helpers -----------------------------------------------------------

    private static String canonicalise(String rawExpr) {
        try {
            return MathExprIO.serialize(MathExpr.parse(rawExpr).tree());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                "cannot parse expression from ModOutput.txt: " + rawExpr, e);
        }
    }

    private static List<JeneticsIO.ModResult> topNByFitness(
        List<JeneticsIO.ModResult> results, int n
    ) {
        List<JeneticsIO.ModResult> sorted = new ArrayList<>(results);
        sorted.sort(Comparator.comparingDouble(JeneticsIO.ModResult::fitness).reversed());
        return new ArrayList<>(sorted.subList(0, n));
    }
}
