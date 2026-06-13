package fitnessevolution;

import io.jenetics.Genotype;
import io.jenetics.Mutator;
import io.jenetics.Phenotype;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.ProgramChromosome;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.MathExpr;
import io.jenetics.prog.op.Op;
import io.jenetics.prog.op.Var;
import io.jenetics.util.ISeq;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generational GP driver with manual step control so fitness can come from an
 * external process. See ROADMAP.md for the locked algorithm.
 */
public final class GPEngine {

    public static final double CROSSOVER_PROB = 1.0;
    public static final double MUTATION_PROB = 0.1;
    public static final int TOURNAMENT_K = 3;
    public static final double ELITE_FRACTION = 0.10;
    public static final double PARENT_POOL_FRACTION = 0.50;

    static final int DEDUPE_MUTATE_ATTEMPTS = 3;
    static final int DEDUPE_RANDOM_ATTEMPTS = 10;

    private final ISeq<Op<Double>> operations;
    private final ISeq<Op<Double>> terminals;
    private final int maxDepth;
    private final int populationSize;
    private final Random rng;

    private final SingleNodeCrossover<ProgramGene<Double>, Double> crossover =
        new SingleNodeCrossover<>(1.0);
    private final Mutator<ProgramGene<Double>, Double> mutator =
        new Mutator<>(1.0);

    public GPEngine(
        ISeq<Op<Double>> operations,
        ISeq<Op<Double>> terminals,
        int maxDepth,
        int populationSize,
        long seed
    ) {
        this.operations = operations;
        this.terminals = terminals;
        this.maxDepth = maxDepth;
        this.populationSize = populationSize;
        this.rng = new Random(seed);
    }

    public int populationSize() {
        return populationSize;
    }

    /**
     * 1 template individual + (populationSize - 1) random trees at ramped depths.
     */
    public List<Genotype<ProgramGene<Double>>> initialPopulation(
        TreeNode<Op<Double>> template,
        ISeq<Var<Double>> canonicalVars
    ) {
        List<Genotype<ProgramGene<Double>>> pop = new ArrayList<>(populationSize);

        TreeNode<Op<Double>> normalized = normalizeVars(template, canonicalVars);
        pop.add(Genotype.of(ProgramChromosome.of(normalized, operations, terminals)));

        pop.addAll(randomTrees(populationSize - 1));
        return pop;
    }

    /**
     * Generate {@code count} random individuals at ramped depths in
     * {@code [2, maxDepth]}. Used by {@link Main} to fill a partial ModOutput
     * up to the configured population size.
     */
    public List<Genotype<ProgramGene<Double>>> randomTrees(int count) {
        List<Genotype<ProgramGene<Double>>> pop = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int depth = 2 + (i % Math.max(1, maxDepth - 1));
            pop.add(Genotype.of(ProgramChromosome.of(depth, operations, terminals)));
        }
        return pop;
    }

    /**
     * Re-hydrate a population from its serialized expressions. Parses each
     * MathExpr string, rewrites its Var leaves to the canonical Var instances,
     * and wraps it in a Genotype. Used by the one-shot {@link Main} when
     * resuming from a persisted state file.
     */
    public List<Genotype<ProgramGene<Double>>> rehydratePopulation(
        List<String> expressions,
        ISeq<Var<Double>> canonicalVars
    ) {
        List<Genotype<ProgramGene<Double>>> pop = new ArrayList<>(expressions.size());
        for (int i = 0; i < expressions.size(); i++) {
            String expr = expressions.get(i);
            TreeNode<Op<Double>> tree;
            try {
                tree = TreeNode.ofTree(MathExpr.parse(expr).tree());
            } catch (RuntimeException e) {
                throw new IllegalArgumentException(
                    "cannot parse expression at index " + i + ": " + expr, e);
            }
            TreeNode<Op<Double>> normalized = normalizeVars(tree, canonicalVars);
            pop.add(Genotype.of(ProgramChromosome.of(normalized, operations, terminals)));
        }
        return pop;
    }

    public List<Genotype<ProgramGene<Double>>> step(List<Scored> scored) {
        if (scored.size() != populationSize) {
            throw new IllegalArgumentException(
                "expected " + populationSize + " scored individuals, got " + scored.size());
        }

        List<Scored> sorted = new ArrayList<>(scored);
        sorted.sort(Comparator.comparingDouble(Scored::fitness).reversed());

        int eliteCount = Math.max(1, (int) Math.round(populationSize * ELITE_FRACTION));
        int poolSize = Math.max(2, (int) Math.round(populationSize * PARENT_POOL_FRACTION));
        List<Scored> pool = sorted.subList(0, poolSize);

        List<Genotype<ProgramGene<Double>>> next = new ArrayList<>(populationSize);
        for (int i = 0; i < eliteCount; i++) {
            next.add(sorted.get(i).genotype());
        }
        while (next.size() < populationSize) {
            next.add(reproduce(pool));
        }
        return dedupe(next);
    }

    /**
     * Replace any individual whose canonical expression matches an earlier one
     * in the list. First tries up to {@link #DEDUPE_MUTATE_ATTEMPTS} mutations
     * of the duplicate, then falls back to fresh random trees. Order-stable:
     * earlier entries (elites, then reproductions in birth order) win
     * collisions.
     */
    List<Genotype<ProgramGene<Double>>> dedupe(
        List<Genotype<ProgramGene<Double>>> pop
    ) {
        Set<String> seen = new HashSet<>();
        List<Genotype<ProgramGene<Double>>> out = new ArrayList<>(pop.size());
        for (Genotype<ProgramGene<Double>> g : pop) {
            if (seen.add(canonical(g))) {
                out.add(g);
                continue;
            }
            Genotype<ProgramGene<Double>> replacement = g;
            boolean placed = false;
            for (int i = 0; i < DEDUPE_MUTATE_ATTEMPTS && !placed; i++) {
                replacement = applyMutation(replacement);
                if (seen.add(canonical(replacement))) {
                    out.add(replacement);
                    placed = true;
                }
            }
            for (int i = 0; i < DEDUPE_RANDOM_ATTEMPTS && !placed; i++) {
                Genotype<ProgramGene<Double>> fresh = randomTrees(1).get(0);
                if (seen.add(canonical(fresh))) {
                    out.add(fresh);
                    placed = true;
                }
            }
            if (!placed) {
                out.add(g);
            }
        }
        return out;
    }

    private static String canonical(Genotype<ProgramGene<Double>> g) {
        return MathExprIO.serialize(g.gene().toTreeNode());
    }

    private Genotype<ProgramGene<Double>> reproduce(List<Scored> pool) {
        Genotype<ProgramGene<Double>> a = tournament(pool).genotype();
        Genotype<ProgramGene<Double>> b = tournament(pool).genotype();

        Genotype<ProgramGene<Double>> child = (rng.nextDouble() < CROSSOVER_PROB)
            ? applyCrossover(a, b)
            : a;

        if (rng.nextDouble() < MUTATION_PROB) {
            child = applyMutation(child);
        }
        return child;
    }

    private Scored tournament(List<Scored> pool) {
        Scored best = null;
        for (int i = 0; i < TOURNAMENT_K; i++) {
            Scored c = pool.get(rng.nextInt(pool.size()));
            if (best == null || c.fitness() > best.fitness()) {
                best = c;
            }
        }
        return best;
    }

    private Genotype<ProgramGene<Double>> applyCrossover(
        Genotype<ProgramGene<Double>> a,
        Genotype<ProgramGene<Double>> b
    ) {
        ISeq<Phenotype<ProgramGene<Double>, Double>> pop = ISeq.of(
            Phenotype.of(a, 0L),
            Phenotype.of(b, 0L)
        );
        return crossover.alter(pop, 0L).population().get(0).genotype();
    }

    private Genotype<ProgramGene<Double>> applyMutation(Genotype<ProgramGene<Double>> g) {
        ISeq<Phenotype<ProgramGene<Double>, Double>> pop = ISeq.of(Phenotype.of(g, 0L));
        return mutator.alter(pop, 0L).population().get(0).genotype();
    }

    /**
     * Replace every Var leaf with the canonical Var of the same name so
     * ProgramChromosome accepts the tree against the terminals set we configured.
     */
    private static TreeNode<Op<Double>> normalizeVars(
        TreeNode<Op<Double>> tree,
        ISeq<Var<Double>> canonicalVars
    ) {
        Map<String, Var<Double>> byName = canonicalVars.stream()
            .collect(Collectors.toMap(Var::name, v -> v));
        return tree.map(op -> {
            if (op instanceof Var<?> v) {
                Var<Double> canonical = byName.get(v.name());
                if (canonical == null) {
                    throw new IllegalArgumentException(
                        "template references unknown variable: " + v.name());
                }
                return canonical;
            }
            return op;
        });
    }
}
