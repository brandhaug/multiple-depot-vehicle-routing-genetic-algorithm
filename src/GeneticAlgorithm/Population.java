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
    private List<Individual> individuals = new ArrayList<>();

    private Individual alphaIndividual; // Best Individual (with best fitness)

    private int generation = 0; // Increment after each tick() loop

    private int populationSize; // Number of Solutions in population
    private double crossOverRate;
    private double mutationRate;
    private int tournamentSize;
    private double selectionRate;
    private int numberOfChildren;
    private int penaltyRate;


    /**
     * Sets parameters
     * Generates initial population which generates n random Solutions. n = populationSize
     *  @param depots
     * @param populationSize
     * @param crossOverRate
     * @param mutationRate
     * @param selectionRate
     * @param tournamentSize
     * @param penaltyRate
     */
    public Population(List<Depot> depots, int populationSize, double crossOverRate, double mutationRate, double selectionRate, int tournamentSize, int numberOfChildren, int penaltyRate) {
        this.depots = depots;
        this.populationSize = populationSize;
        this.crossOverRate = crossOverRate;
        this.mutationRate = mutationRate;
        this.selectionRate = selectionRate;
        this.tournamentSize = tournamentSize;
        this.numberOfChildren = numberOfChildren;
        this.penaltyRate = penaltyRate;
    }

    /**
     * One generation of Population
     * Loops through one generation of each Individual
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
            List<Individual> children = new ArrayList<>();
//            List<Individual> parentsToRemove = new ArrayList<>(); // TODO: Should we try to not remove them to enable elitism?

            for (int i = 0; i < individuals.size(); i++) { // Would this actually make 2*numberOfChildren? Yes
                Individual[] parents = selection();
                Individual[] crossOverChildren = crossOver(parents);

                if (crossOverChildren == null) {
                    crossOverChildren = parents;
                }

                children.addAll(List.of(crossOverChildren[0], crossOverChildren[1]));
//                parentsToRemove.addAll(List.of(parents[0], parents[1]));
            }

            List<Individual> childrenToAdd = new ArrayList<>();
            for (Individual child : children) {
                double random = Utils.randomDouble();
                if (random < mutationRate) {
                    Individual mutatedChild = new Individual(depots, penaltyRate, child.mutation());
                    childrenToAdd.add(mutatedChild);
                } else {
                    childrenToAdd.add(child);
                }
            }

//            individuals.removeAll(parentsToRemove);
            individuals.addAll(childrenToAdd);
            individuals.sort(Comparator.comparingDouble(Individual::getFitness)); // Sort by fitness
            individuals = individuals.stream().limit(populationSize).collect(Collectors.toList()); // Cut population to population size
        }
        generation++;
    }

    /**
     * Generates initial population which generates n random Solutions. n = populationSize
     */
    private void generateInitialPopulation() {
        int triesLeft = 1000;
        boolean force = false;

        while (individuals.size() != populationSize) {
            if (triesLeft == 0) {
                force = true;
            }

            Individual individual = new Individual(depots, penaltyRate);
            boolean successful = individual.generateRandomIndividual();

            if (successful || force) {
                individuals.add(individual);
            } else {
                triesLeft--;
            }
        }

        // Hard maps initialSolution: 7, 11, 16, 17, 19, 20, 22, 23
        // Hard maps initialSolution2: 7, 8, 9, 10, 11, 16, 17, 19, 20, 22, 23
        if (triesLeft == 0) {
//            throw new Error("Generating initial population failed - created " + individuals.size() + " of " + populationSize + " individuals");
            System.out.println("Generated population with constraint break");
        }

        if (Controller.verbose) {
            System.out.println("Initial population of " + populationSize + " generated with " + triesLeft + " tries left");
        }
    }

    private Individual[] crossOver(Individual[] parents) {
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

            List<Vehicle> child1Vehicles = parents[0].crossOverNoConstraints(routesFromS2[0]);
            List<Vehicle> child2Vehicles = parents[0].crossOverNoConstraints(routesFromS2[1]);
            List<Vehicle> child3Vehicles = parents[1].crossOverNoConstraints(routesFromS1[0]);
            List<Vehicle> child4Vehicles = parents[1].crossOverNoConstraints(routesFromS1[1]);

            if (child1Vehicles != null || child2Vehicles != null) {
                Individual child1 = new Individual(depots, penaltyRate, child1Vehicles);
                Individual child2 = new Individual(depots, penaltyRate, child2Vehicles);
                Individual child3 = new Individual(depots, penaltyRate, child3Vehicles);
                Individual child4 = new Individual(depots, penaltyRate, child4Vehicles);
                return new Individual[]{child1, child2, child3, child4};
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

    private Individual[] selection() {
        Individual parent1 = tournament();
        Individual parent2 = null;
        while (parent2 == null || parent1 == parent2) {
            parent2 = tournament();
        }

        return new Individual[]{parent1, parent2};
    }

    private Individual tournament() {
        List<Individual> tournamentMembers = new ArrayList<>();

        for (int i = 0; i < tournamentSize; i++) {
            boolean contained = true;
            Individual member = null;
            while (contained) {
                int randomIndex = Utils.randomIndex(populationSize);
                member = individuals.get(randomIndex);
                contained = tournamentMembers.contains(member);
            }
            tournamentMembers.add(member);
        }
        tournamentMembers.sort(Comparator.comparingDouble(Individual::getFitness));
        return tournamentMembers.get(0);
    }

    public double getAlphaDuration() {
        return alphaIndividual.getDuration();
    }

    public double getAlphaFitness() {
        return alphaIndividual.getFitness();
    }

    public Individual getAlphaIndividual() {
        if (individuals.size() == 0) {
            return null;
        }

        individuals.sort(Comparator.comparingDouble(Individual::getFitness)); // Sorts based on fitness
        alphaIndividual = individuals.get(0); // Best Individual
        return alphaIndividual;
    }

    public int getGeneration() {
        return generation;
    }

    public void reset() {
        alphaIndividual = null;
        individuals = null;
        generation = 0;
    }

    public boolean isAlphaValid() {
        return alphaIndividual.isValid();
    }
}
