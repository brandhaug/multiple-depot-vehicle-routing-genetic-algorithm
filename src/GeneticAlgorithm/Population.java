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
    private int populationSize; // Number of Solutions in population
    private int generation = 0; // Increment after each tick() loop

    private double crossOverRate;
    private double mutationRate;
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
        this.crossOverRate = crossOverRate;
        this.mutationRate = mutationRate;
        this.selectionRate = selectionRate;
        generateInitialPopulation();

    }

    /**
     * One generation of Population
     * Loops through one generation of each Solution
     * <p>
     * One generation of solution
     * 1. Crossover
     * 2. Mutation
     * 3. Calculate distance and fitness
     */
    public void tick() {
        List<Solution> children = new ArrayList<>();
        for (Solution solution : solutions) {
            double random = Utils.randomDouble();
            if (random < crossOverRate) {
                crossOver(solution, children);
            }
            if (random < mutationRate) {
                children.add(new Solution(depots, solution.mutation()));
            }
        }
        solutions.addAll(children);
        // Sort by fitness
        solutions.sort(Comparator.comparingDouble(Solution::getFitness));
        // Cut population to population size
        solutions = solutions.stream().limit(populationSize).collect(Collectors.toList());

        generation++;
    }

    private void crossOver(Solution solution, List<Solution> children) {
        boolean routeAdded = false;
        int triesLeft = 1000;

        while (!routeAdded && triesLeft > 0) {
            Solution partner = findCrossOverPartner(solution);
            List<Vehicle> solutionRoutes = solution.getVehicles();
            Vehicle solutionRandomVehicle = solutionRoutes.get(Utils.randomIndex(solution.getVehicles().size()));
            List<Vehicle> partnerRoutes = partner.getVehicles();
            Vehicle partnerRandomVehicle = partnerRoutes.get(Utils.randomIndex(partner.getVehicles().size()));
            List<Customer> routeFromS1 = new ArrayList<>(solutionRandomVehicle.getRoute());
            List<Customer> routeFromS2 = new ArrayList<>(partnerRandomVehicle.getRoute());

            List<Vehicle> child1Vehicles = solution.crossOver(routeFromS2);
            List<Vehicle> child2Vehicles = partner.crossOver(routeFromS1);
            if (child1Vehicles != null || child2Vehicles != null) {
                Solution child1 = new Solution(depots, child1Vehicles);
                Solution child2 = new Solution(depots, child2Vehicles);

                children.add(child1);
                children.add(child2);
                routeAdded = true;
            }
            else {
                triesLeft--;
            }



        }
    }

    Solution findCrossOverPartner(Solution self) {
        Solution partner = self;
        while (self == partner) {
            partner = solutions.get(Utils.randomIndex(solutions.size()));
        }
        return partner;
    }

    /**
     * Generates initial population which generates n random Solutions. n = populationSize
     */
    private void generateInitialPopulation() {
        while (solutions.size() != populationSize) {
            Solution solution = new Solution(this.depots);
            boolean successful = solution.generateInitialSolution();
            if (successful) solutions.add(solution);
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

        //solutions.sort(Comparator.comparingDouble(Solution::getFitness)); // Sorts based on fitness
        //this.alphaSolution = solutions.get(0); // Best Solution

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

    public double getAlphaFitness() {
        return alphaSolution.getFitness();
    }

    public Solution getAlphaSolution() {
        solutions.sort(Comparator.comparingDouble(Solution::getFitness)); // Sorts based on fitness
        alphaSolution = solutions.get(0); // Best Solution
        return alphaSolution;
    }

    public int getGeneration() {
        return generation;
    }
}
