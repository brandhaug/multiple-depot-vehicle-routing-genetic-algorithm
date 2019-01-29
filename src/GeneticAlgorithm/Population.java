package GeneticAlgorithm;

import Main.Controller;
import MapObjects.Customer;
import MapObjects.Depot;
import Utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Population contains n Solutions. n = populationSize
 */
public class Population
{
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
    public Population(List<Depot> depots, int populationSize, double crossOverRate, double mutationRate, double selectionRate)
    {
        this.depots = depots;
        this.populationSize = populationSize;
        this.crossOverRate = crossOverRate;
        this.mutationRate = mutationRate;
        this.selectionRate = selectionRate;
        generateInitialPopulation(populationSize, crossOverRate, mutationRate);

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
    public void tick()
    {
        //selection();

        List<Solution> children = new ArrayList<>();
        for (Solution solution : solutions)
        {
            double random = Utils.randomDouble();
            if (random < crossOverRate)
            {
                //solutions.add(new Solution(depots, solution.crossOver())); // parents need to be removed, otherwise two new cars is always added to a solution
                Solution partner = findCrossOverPartner(solution);

                // TODO: Unngå at ruter med size 0 ikke får bli med i crossover
                List<Customer> routeFromS1 = solution.getVehicles().get(Utils.randomIndex(solution.getVehicles().size())).getRoute();
                while (routeFromS1.size() == 0) {
                    routeFromS1 = solution.getVehicles().get(Utils.randomIndex(solution.getVehicles().size())).getRoute();
                }
                List<Customer> routeFromS2 = partner.getVehicles().get(Utils.randomIndex(partner.getVehicles().size())).getRoute();
                while (routeFromS2.size() == 0) {
                    routeFromS2 = partner.getVehicles().get(Utils.randomIndex(partner.getVehicles().size())).getRoute();
                }
                children.add(new Solution(depots, solution.crossOver(routeFromS2)));
                children.add(new Solution(depots, partner.crossOver(routeFromS1)));
            }
            if (random < mutationRate)
            {
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

    Solution findCrossOverPartner(Solution self)
    {
        Solution partner = self;
        while (self == partner) {
        partner = solutions.get(Utils.randomIndex(solutions.size() - 1));
    }
        return partner;
    }

    /**
     * Generates initial population which generates n random Solutions. n = populationSize
     *
     * @param populationSize
     * @param crossOverRate
     * @param mutationRate
     */
    private void generateInitialPopulation(int populationSize, double crossOverRate, double mutationRate)
    {
        for (int i = 0; i < populationSize; i++)
        {
            Solution solution = new Solution(this.depots);
            solutions.add(solution);
        }
    }

    /**
     * Sorts the Solutions based on fitness
     * Selects n best Solutions. n = populationRoute to keep populationSize fixed
     * TODO: Select parents for next generation before the the population is filtered
     */
    public void selection()
    {
        if (Controller.verbose)
        {
            System.out.println("========= Performing selection on vehicles =========");
            System.out.println("Vehicles size before selection: " + solutions.size());
        }

        //solutions.sort(Comparator.comparingDouble(Solution::getFitness)); // Sorts based on fitness
        //this.alphaSolution = solutions.get(0); // Best Solution

        // TODO: Select parents
        for (int i = 0; i < solutions.size() * selectionRate; i++)
        {
            Solution parent = solutions.get(i);
        }

        // Filters Solutions to populationSize. Because the list is sorted, the Solutions with worst fitness will be filtered
        solutions = solutions.stream().limit(populationSize).collect(Collectors.toList());

        if (Controller.verbose)
        {
            System.out.println("Sorted solutions distances: " + solutions.toString());
            System.out.println("Solutions size before selection: " + solutions.size());
            System.out.println("========= END Performing selection on solutions =========");
        }
    }

    public int getAlphaFitness()
    {
        return alphaSolution.getFitness();
    }

    public Solution getAlphaSolution()
    {
        solutions.sort(Comparator.comparingDouble(Solution::getFitness)); // Sorts based on fitness
        alphaSolution = solutions.get(0); // Best Solution
        return alphaSolution;
    }

    public int getGeneration()
    {
        return generation;
    }
}
