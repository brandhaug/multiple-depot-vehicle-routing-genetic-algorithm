package GeneticAlgorithm;

import Main.Controller;
import MapObjects.Customer;
import MapObjects.Depot;
import MapObjects.Vehicle;
import Utils.Utils;

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

    private int generation = 0; // Increment after each tick() loop

    private int populationSize; // Number of Solutions in population
    private double crossOverRate;
    private double mutationRate;
    private int tournamentSize;
    private double selectionRate;
    private int numberOfChildren;


    /**
     * Sets parameters
     * Generates initial population which generates n random Solutions. n = populationSize
     *
     * @param depots
     * @param populationSize
     * @param crossOverRate
     * @param mutationRate
     * @param selectionRate
     * @param tournamentSize
     */
    public Population(List<Depot> depots, int populationSize, double crossOverRate, double mutationRate, double selectionRate, int tournamentSize, int numberOfChildren) {
        this.depots = depots;
        this.populationSize = populationSize;
        this.crossOverRate = crossOverRate;
        this.mutationRate = mutationRate;
        this.selectionRate = selectionRate;
        this.tournamentSize = tournamentSize;
        this.numberOfChildren = numberOfChildren;
    }

    /**
     * One generation of Population
     * Loops through one generation of each Solution
     * 1. Crossover
     * 2. Mutation
     * 3. Calculate distance and fitness
     */
    public void tick() {
        if (generation == 0) {
            generateInitialPopulation();
        } else {
            List<Solution> children = new ArrayList<>();
            List<Solution> parentsToRemove = new ArrayList<>(); // Should we try to not remove them to enable elitism?

            for (int i = 0; i < numberOfChildren; i++) { // Would this actually make 2*numberOfChildren? Yes
                Solution[] parents = selection();
                Solution[] crossOverChildren = crossOver(parents);

                if (crossOverChildren == null) {
                    crossOverChildren = parents;
                }

                children.addAll(List.of(crossOverChildren[0], crossOverChildren[1]));
                parentsToRemove.addAll(List.of(parents[0], parents[1]));
            }

            List<Solution> mutatedChildren = new ArrayList<>();
            for (Solution child : children) {
                double random = Utils.randomDouble();
                if (random < mutationRate) {
                    Solution mutatedChild = new Solution(depots, child.mutation());
                    mutatedChildren.add(mutatedChild);
                } else {
//                mutatedChildren.add(child);
                }
            }

            solutions.removeAll(parentsToRemove);
            solutions.addAll(mutatedChildren);
            solutions.sort(Comparator.comparingDouble(Solution::getFitness)); // Sort by fitness
            solutions = solutions.stream().limit(populationSize).collect(Collectors.toList()); // Cut population to population size
        }
        generation++;
    }

    private Solution[] crossOver(Solution[] parents) {
        int triesLeft = 1000;

        while (triesLeft > 0) {
            List<Vehicle> solutionVehicles = parents[0].getVehicles();
            if (solutionVehicles == null) throw new NullPointerException("SolutionRoutes is null, you suck");
            int randIndex = Utils.randomIndex(solutionVehicles.size());
            Vehicle solutionVehicle = solutionVehicles.get(randIndex);

            List<Vehicle> partnerVehicles = parents[1].getVehicles();
            if (partnerVehicles == null) throw new NullPointerException("PartnerRoutes is null, you suck");
            int randIndex2 = Utils.randomIndex(parents[1].getVehicles().size());

            Vehicle partnerVehicle = partnerVehicles.get(randIndex2);
            List<Customer> routeFromS1 = new ArrayList<>(solutionVehicle.getRoute());
            List<Customer> routeFromS2 = new ArrayList<>(partnerVehicle.getRoute());

            List<Vehicle> child1Vehicles = parents[0].crossOver(routeFromS2);
            List<Vehicle> child2Vehicles = parents[1].crossOver(routeFromS1);

            if (child1Vehicles != null || child2Vehicles != null) {
                Solution child1 = new Solution(depots, child1Vehicles);
                Solution child2 = new Solution(depots, child2Vehicles);
                return new Solution[]{child1, child2};
            } else {
                triesLeft--;
            }
        }
        return null;
    }

    /**
     * Generates initial population which generates n random Solutions. n = populationSize
     */
    private void generateInitialPopulation() {
        int triesLeft = 10000;

        while (solutions.size() != populationSize && triesLeft != 0) {

            Solution solution = new Solution(depots);
            boolean successful = solution.generateInitialSolution();
            if (!successful) {
                triesLeft--;
            } else {
                solutions.add(solution);
            }
        }

        // Hard maps: 7, 8, 9, 10, 11, 16, 17, 19, 20, 22, 23
        if (solutions.size() != populationSize) {
            throw new Error("Generating initial population failed - created " + solutions.size() + " of " + populationSize + " solutions");
        }

        if (Controller.verbose) {
            System.out.println("Initial population of " + populationSize + " generated with " + triesLeft + " tries left");
        }
    }

    private Solution[] selection() {
        Solution parent1 = tournament();
        Solution parent2 = null;
        while (parent2 == null || parent1 == parent2) {
            parent2 = tournament();
        }

        return new Solution[]{parent1, parent2};
    }

    private Solution tournament() {
        List<Solution> tournamentMembers = new ArrayList<>();

        for (int i = 0; i < tournamentSize; i++) {
            boolean contained = true;
            Solution member = null;
            while (contained) {
                int randomIndex = Utils.randomIndex(populationSize);
                member = solutions.get(randomIndex);
                contained = tournamentMembers.contains(member);
            }
            tournamentMembers.add(member);
        }
        tournamentMembers.sort(Comparator.comparingDouble(Solution::getFitness));
        return tournamentMembers.get(0);
    }

    public double getAlphaFitness() {
        return alphaSolution.getFitness();
    }

    public Solution getAlphaSolution() {
        if (solutions.size() == 0) {
            return null;
        }

        solutions.sort(Comparator.comparingDouble(Solution::getFitness)); // Sorts based on fitness
        alphaSolution = solutions.get(0); // Best Solution
        return alphaSolution;
    }

    public int getGeneration() {
        return generation;
    }
}
