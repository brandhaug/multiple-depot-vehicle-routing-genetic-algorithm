package GeneticAlgorithm;

import MapObjects.Depot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Population {
    private List<Depot> depots;
    private List<Solution> solutions = new ArrayList<>();
    private Solution alphaSolution;
    private int populationSize;
    private double selectionRate;

    public Population(List<Depot> depots, int populationSize, double crossOverRate, double mutationRate, double selectionRate) {
        this.depots = depots;
        this.populationSize = populationSize;
        this.selectionRate = selectionRate;
        generateInitialPopulation(populationSize, crossOverRate, mutationRate);
    }

    public void tick() {
        selection();

        for (Solution solution : solutions) {
            solution.tick();
        }

    }

    private void generateInitialPopulation(int populationSize, double crossOverRate, double mutationRate) {
        for (int i = 0; i < populationSize; i++) {
            Solution solution = new Solution(this.depots, crossOverRate, mutationRate);
            solutions.add(solution);
        }
    }

    /**
     * Sorts the solutions based on fitness and selects n best routes
     */
    public void selection() {
        System.out.println("========= Performing selection on vehicles =========");
        System.out.println("Vehicles size before selection: " + solutions.size());

        solutions.sort(Comparator.comparingDouble(Solution::getFitness));
        this.alphaSolution = solutions.get(0);

        for (int i = 0; i < solutions.size() * selectionRate; i++) {
            Solution solution = solutions.get(i);
        }

        System.out.println("Sorted solutions distances: " + solutions.toString());
        solutions = solutions.stream().limit(populationSize).collect(Collectors.toList());
        System.out.println("Solutions size before selection: " + solutions.size());
        System.out.println("========= END Performing selection on solutions =========");
    }

    public int getAlphaFitness() {
        return alphaSolution.getFitness();
    }

    public Solution getAlphaSolution() {
        return alphaSolution;
    }
}
