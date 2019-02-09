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
     * 1. Selection
     * 2. Crossover
     * 3. Mutation
     * 4. Calculate distance and fitness
     * 5. Filtering
     */
    public void tick() {
        if (generation == 0) {
            generateInitialPopulation();
        } else {
            List<Solution> children = new ArrayList<>();
            List<Solution> parentsToRemove = new ArrayList<>(); // Should we try to not remove them to enable elitism?

            for (int i = 0; i < numberOfChildren; i++) { // Would this actually make 2*numberOfChildren? Yes
                Solution[] parents = selection();
                Solution[] crossOverChildren;

                // Parents get to crossover if random is less than crossOverRate
                double random = Utils.randomDouble();
                if (random < crossOverRate) {
                    crossOverChildren = crossOver(parents);
                    if (crossOverChildren == null) {
                        crossOverChildren = parents;
                    }
                }
                else {
                    crossOverChildren = parents;
                }

                children.addAll(List.of(crossOverChildren[0], crossOverChildren[1]));
                parentsToRemove.addAll(List.of(parents[0], parents[1]));
            }

            // Mutates children
            List<Solution> childrenToAdd = new ArrayList<>();
            for (Solution child : children) {
                double random = Utils.randomDouble();
                if (random < mutationRate) {
                    Solution mutatedChild;
                    if (Utils.randomDouble() < 0.5)
                        mutatedChild = new Solution(depots, child.mutation2());
                    else
                        mutatedChild = new Solution(depots, child.mutation());
                    childrenToAdd.add(mutatedChild);
                } else {
                    childrenToAdd.add(child);
                }
            }

            /*
            Before: Remove parents, add children, sort, cut to populationSize
            solutions.removeAll(parentsToRemove);
            solutions.addAll(childrenToAdd);
            solutions.sort(Comparator.comparingDouble(Solution::getFitness)); // Sort by fitness
            solutions = solutions.stream().limit(populationSize).collect(Collectors.toList()); // Cut population to population size
             */
            /*
            Now: Decide that the populationSize/20 best solutions from the previous generation get to survive
            if they have better fitness than the populationSize/20 worst children
            */

            solutions.removeAll(parentsToRemove);
            solutions.sort(Comparator.comparingDouble(Solution::getFitness)); // Sort by fitness
            List<Solution> parentsToSave = new ArrayList<>(solutions.subList(0, populationSize/20));
            solutions = childrenToAdd;
            solutions.addAll(parentsToSave);
            solutions.sort(Comparator.comparingDouble(Solution::getFitness)); // Sort by fitness
            solutions = solutions.stream().limit(populationSize).collect(Collectors.toList()); // Cut population to population size
        }
        generation++;
    }

    /**
     * Generates initial population which generates n random Solutions. n = populationSize
     */
    private void generateInitialPopulation() {
        int triesLeft = 1000;
        boolean force = false;

        while (solutions.size() != populationSize) {
            if (triesLeft == 0) {
                force = true;
            }

            Solution solution = new Solution(depots);
            boolean successful = solution.generateInitialSolution(force);

            if (successful || force) {
                solutions.add(solution);
            } else {
                triesLeft--;
            }
        }

        // Hard maps initialSolution: 7, 11, 16, 17, 19, 20, 22, 23
        // Hard maps initialSolution2: 7, 8, 9, 10, 11, 16, 17, 19, 20, 22, 23
        if (triesLeft == 0) {
//            throw new Error("Generating initial population failed - created " + solutions.size() + " of " + populationSize + " solutions");
            System.out.println("Generated population with constraint break");
        }

        if (Controller.verbose) {
            System.out.println("Initial population of " + populationSize + " generated with " + triesLeft + " tries left");
        }
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
            List<Customer>[] routesFromS1 = splitRoute(solutionVehicle.getRoute());
            List<Customer>[] routesFromS2 = splitRoute(partnerVehicle.getRoute());

            List<Vehicle> child1Vehicles = parents[0].crossOver(routesFromS2[0]);
            List<Vehicle> child2Vehicles = parents[0].crossOver(routesFromS2[1]);
            List<Vehicle> child3Vehicles = parents[1].crossOver(routesFromS1[0]);
            List<Vehicle> child4Vehicles = parents[1].crossOver(routesFromS1[1]);

            //TODO: Check if this is right? Should both child1 and child2 be created if the other is null?
            if (child1Vehicles != null || child2Vehicles != null) {
                // TODO: Check if solution is valid
                Solution child1 = new Solution(depots, child1Vehicles);
                Solution child2 = new Solution(depots, child2Vehicles);
                Solution child3 = new Solution(depots, child3Vehicles);
                Solution child4 = new Solution(depots, child4Vehicles);
                if (child1.isValid() && child2.isValid() && child3.isValid() && child4.isValid())
                    return new Solution[]{child1, child2, child3, child4};
                else
                    triesLeft--;
            } else {
                triesLeft--;
            }
        }
        return null;
    }

    /**
     * Splits route in n parts
     *
     * @param route
     * @return
     */
    private List<Customer>[] splitRoute(List<Customer> route) {
        if (Controller.verbose) {
            System.out.println("========= Splitting route to subRoutes =========");
        }
        List<Customer> first = new ArrayList<>();
        List<Customer> second = new ArrayList<>();
        int size = route.size();

        if (size != 0) {
            int partitionIndex = Utils.randomIndex(size);

            if (Controller.verbose) {
                System.out.println("Partition Index: " + partitionIndex);
            }

            for (int i = 0; i < route.size(); i++) {
                if (partitionIndex > i) {
                    first.add(route.get(i));
                } else {
                    second.add(route.get(i));
                }
            }
        }
        if (Controller.verbose) {
            System.out.println("First subRoute: " + first.toString());
            System.out.println("Second subRoute: " + second.toString());
        }

        if (Controller.verbose) {
            System.out.println("========= END Splitting route to subRoutes =========");
        }

        return new List[]{first, second};
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

    public double getAverageFitness() {
        double fitness = 0.0;
        for (Solution s : solutions) {
            fitness += s.getFitness();
        }
        return fitness / solutions.size();
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

    public void reset() {
        alphaSolution = null;
        solutions = null;
        generation = 0;
    }
}
