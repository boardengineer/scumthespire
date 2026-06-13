package fitnessevolution;

import io.jenetics.Genotype;
import io.jenetics.prog.ProgramGene;

public record Scored(Genotype<ProgramGene<Double>> genotype, double fitness) {
    public String expr() {
        return MathExprIO.serialize(genotype.gene().toTreeNode());
    }
}
