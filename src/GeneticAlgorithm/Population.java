package GeneticAlgorithm;

import Main.Controller;
import MapObjects.Depot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Population contains n Solutions. n = populationSize
 */
public class Population {
    private List<Depot> depots;
    private List<Solution> solutions = new ArrayList<>();
    private Solution alphaSolution; // Best Solution (with best fitness)
    private int populationSize; // Number of Solutions in population
    private int generation = 0; // Increment after each tick() loop
    private double selectionRate;

    /**
     * Sets parameters
     * Generates initial population which generates n random Solutions. n = populationSize
     *
     * @param depots
     * @param populationSize
     * @param crossOverRate
     * @param mutationRate
     * @param selectionRate
     */
    public Population(List<Depot> depots, int populationSize, double crossOverRate, double mutationRate, double selectionRate) {
        this.depots = depots;
        this.populationSize = populationSize;
        this.selectionRate = selectionRate;
        generateInitialPopulation(populationSize, crossOverRate, mutationRate);
    }

    /**
     * One generation of Population
     * Loops through one generation of each Solution
     */
    public void tick() {
        selection();

        for (Solution solution : solutions) {
            solution.tick();
        }

        generation++;

    }

    /**
     * Generates initial population which generates n random Solutions. n = populationSize
     *
     * @param populationSize
     * @param crossOverRate
     * @param mutationRate
     */
    private void generateInitialPopulation(int populationSize, double crossOverRate, double mutationRate) {
        for (int i = 0; i < populationSize; i++) {
            Solution solution = new Solution(this.depots, crossOverRate, mutationRate);
            solutions.add(solution);
        }
    }

    /**
     * Sorts the Solutions based on fitness
     * Selects n best Solutions. n = populationRoute to keep populationSize fixed
     * TODO: Select parents for next generation before the the population is filtered
     */
    public void selection() {
        if (Controller.verbose) {
            System.out.println("========= Performing selection on vehicles =========");
            System.out.println("Vehicles size before selection: " + solutions.size());
        }

        solutions.sort(Comparator.comparingDouble(Solution::getFitness)); // Sorts based on fitness
        this.alphaSolution = solutions.get(0); // Best Solution

        // TODO: Select parents
        for (int i = 0; i < solutions.size() * selectionRate; i++) {
            Solution parent = solutions.get(i);
        }

        // Filters Solutions to populationSize. Because the list is sorted, the Solutions with worst fitness will be filtered
        solutions = solutions.stream().limit(populationSize).collect(Collectors.toList());

        if (Controller.verbose) {
            System.out.println("Sorted solutions distances: " + solutions.toString());
            System.out.println("Solutions size before selection: " + solutions.size());
            System.out.println("========= END Performing selection on solutions =========");
        }
    }

    public int getAlphaFitness() {
        return alphaSolution.getFitness();
    }

    public Solution getAlphaSolution() {
        return alphaSolution;
    }

    public int getGeneration() {
        return generation;
    }
}
