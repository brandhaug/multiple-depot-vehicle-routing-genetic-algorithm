package GeneticAlgorithm;

import MapObjects.Depot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Population {
    private List<Depot> depots;
    private List<Solution> solutions = new ArrayList<>();
    private Solution alphaSolution;

    public Population(List<Depot> depots, int populationSize, double crossOverRate, double mutationRate) {
        this.depots = depots;
        generateInitialPopulation(populationSize, crossOverRate, mutationRate);
    }

    public void tick() {
        for (Solution solution : solutions) {
            solution.tick();
        }

        selection();
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
        System.out.println("Sorted solutions distances: " + solutions.toString());
//        solutions = solutions.stream().limit(this.solutions.size()).collect(Collectors.toList()); //      TODO: Change limit
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
