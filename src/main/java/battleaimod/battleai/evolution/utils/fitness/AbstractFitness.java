package battleaimod.battleai.evolution.utils.fitness;

public abstract class AbstractFitness implements Comparable<AbstractFitness> {


    private double fitness = 0;

    public abstract double evaluate();
    public void setFitnessFitness(double fitness){
        this.fitness = fitness;
    }
    public double getFitnessFitness() {
        return fitness;
    }

    @Override
    public int compareTo(AbstractFitness other) {
        // Higher score = better (sorted descending)
        return Double.compare(other.fitness, this.fitness);
    }
}
