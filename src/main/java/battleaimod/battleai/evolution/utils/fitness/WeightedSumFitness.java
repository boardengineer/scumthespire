package battleaimod.battleai.evolution.utils.fitness;

import battleaimod.battleai.evolution.utils.ValueFunctionManager;

import java.util.Random;

public class WeightedSumFitness extends AbstractFitness implements Comparable<AbstractFitness> {
    private static final Random rand = new Random();
    private final double[] weights;

    public WeightedSumFitness(String line) {
        String[] parts = line.split(",");

        int expectedLength = ValueFunctionManager.Variables.values().length;
        weights = new double[expectedLength];

        int limit = Math.min(parts.length, expectedLength);
        for (int i = 0; i < limit; i++) {
            weights[i] = Double.parseDouble(parts[i]);
        }
        // Remaining values are already 0.0 by default (no need to explicitly set)
    }

    public WeightedSumFitness(WeightedSumFitness parent1, WeightedSumFitness parent2){
        //Crossover Constructor for Uniform Crossover

        weights = parent1.weights.clone();

        for(int i = 0; i < weights.length; i++){
            if(rand.nextBoolean()){
                weights[i] = parent2.weights[i];
            }
        }

    }

    public WeightedSumFitness(WeightedSumFitness old, boolean mutate){
        weights = old.weights.clone();
        if(mutate){
            for(int i = 0; i < weights.length; i++){
                if(rand.nextFloat() < 0.25) weights[i] = mutateWeight(weights[i]);
            }
        }
    }



    @Override
    public double evaluate(){
        double sum = 0;
        ValueFunctionManager.Variables[] vars = ValueFunctionManager.Variables.values();
        for(int i = 0; i < weights.length;i++){
            sum += ValueFunctionManager.getVariableValue(vars[i]) * weights[i];
        }
        return sum;
    }

    public double mutateWeight(double oldWeight) {
        double r = rand.nextDouble();

        // Escape exact zero
        if (oldWeight == 0.0) {
            oldWeight = rand.nextGaussian() * 0.1;
        }

        if (r < 0.9) {
            // Small relative mutation
            double sigma = 0.1;
            oldWeight *= (1 + rand.nextGaussian() * sigma);
        } else {
            // Large mutation (allow sign flip)
            double factor = 0.2 + rand.nextDouble() * 4.8;
            if (rand.nextBoolean()) factor *= -1;
            oldWeight *= factor;
        }

        // Optional additive mutation (helps cross zero smoothly)
        if (rand.nextDouble() < 0.1) {
            oldWeight += rand.nextGaussian() * 0.05;
        }

        // Snap-to-zero logic
        double epsilon = 1e-3;       // "close to zero" threshold
        double zeroChance = 0.3;     // probability of snapping

        if (Math.abs(oldWeight) < epsilon && rand.nextDouble() < zeroChance) {
            oldWeight = 0.0;
        }

        // Symmetric clamp
        double max = 1e6;
        if (oldWeight > max) oldWeight = max;
        if (oldWeight < -max) oldWeight = -max;

        return oldWeight;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < weights.length; i++) {
            sb.append(weights[i]);
            if (i < weights.length - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }


}
