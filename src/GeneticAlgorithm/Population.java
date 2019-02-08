package GeneticAlgorithm;

import Main.Controller;
import MapObjects.Customer;
import MapObjects.Depot;
import MapObjects.Vehicle;
import Utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
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
    private int durationPenaltyRate;
    private int loadPenaltyRate;
    private boolean elitism;
    private boolean forceLoadConstraint;

    /**
     * Sets parameters
     * Generates initial population which generates n random Solutions. n = populationSize
     * @param depots
     * @param populationSize
     * @param crossOverRate
     * @param mutationRate
     * @param selectionRate
     * @param tournamentSize
     * @param durationPenaltyRate
     * @param loadPenaltyRate
     * @param forceLoadConstraint
     */
    public Population(List<Depot> depots,
                      int populationSize,
                      double crossOverRate,
                      double mutationRate,
                      double selectionRate,
                      int tournamentSize,
                      int numberOfChildren,
                      int durationPenaltyRate,
                      int loadPenaltyRate,
                      boolean elitism,
                      boolean forceLoadConstraint) {
        this.depots = depots;
        this.populationSize = populationSize;
        this.crossOverRate = crossOverRate;
        this.mutationRate = mutationRate;
        this.selectionRate = selectionRate;
        this.tournamentSize = tournamentSize;
        this.numberOfChildren = numberOfChildren;
        this.durationPenaltyRate = durationPenaltyRate;
        this.loadPenaltyRate = loadPenaltyRate;
        this.elitism = elitism;
        this.forceLoadConstraint = forceLoadConstraint;
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
            List<Individual> parentsToRemove = new ArrayList<>();

            for (int i = 0; i < numberOfChildren; i++) { // Would this actually make 2*numberOfChildren?
                Individual[] parents = selection();
                Individual[] crossOverChildren;

                // Parents get to crossover if random is less than crossOverRate
                double random = Utils.randomDouble();
                if (random < crossOverRate) {
                    crossOverChildren = crossOver(parents);
                    if (crossOverChildren == null) {
                        crossOverChildren = parents;
                    }
                } else {
                    crossOverChildren = parents;
                }

                children.addAll(List.of(crossOverChildren[0], crossOverChildren[1]));

                if (!elitism) {
                    parentsToRemove.addAll(Arrays.asList(parents[0], parents[1]));
                }
            }

            List<Individual> childrenToAdd = new ArrayList<>();
            for (Individual child : children) {
                double random = Utils.randomDouble();
                if (random < mutationRate) {
                    // TODO: Optimize parameters
//                    Individual mutatedChild = new Individual(depots, durationPenaltyRate, loadPenaltyRate, child.swapMutation());
                    Individual mutatedChild = new Individual(depots, durationPenaltyRate, loadPenaltyRate, child.crossMutation());
                    childrenToAdd.add(mutatedChild);
                } else {
                    childrenToAdd.add(child);
                }
            }

            /*
            Before: Remove parents, add children, sort, cut to populationSize
            individuals.removeAll(parentsToRemove);
            individuals.addAll(childrenToAdd);
            individuals.sort(Comparator.comparingDouble(Solution::getFitness)); // Sort by fitness
            individuals = solutions.stream().limit(populationSize).collect(Collectors.toList()); // Cut population to population size
             */
            /*
            Now: Decide that the populationSize/20 best solutions from the previous generation get to survive
            if they have better fitness than the populationSize/20 worst children
            */
            if (!elitism) {
                individuals.removeAll(parentsToRemove);
            }
            individuals.sort(Comparator.comparingDouble(Individual::calculateFitness)); // Sort by fitness
            List<Individual> parentsToSave = new ArrayList<>(individuals.subList(0, populationSize/20));
            individuals = childrenToAdd;
            individuals.addAll(parentsToSave);
            individuals.sort(Comparator.comparingDouble(Individual::calculateFitness)); // Sort by fitness
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

            Individual individual = new Individual(depots, durationPenaltyRate, loadPenaltyRate);

            // TODO: Parameter optimize
//            boolean successful = individual.generateOptimizedIndividual(force);
            boolean successful = individual.generateOptimizedIndividual2();
//            boolean successful = individual.generateRandomIndividual();

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
            List<Customer>[] routesFromS1 = Utils.splitRoute(solutionVehicle.getRoute());
            List<Customer>[] routesFromS2 = Utils.splitRoute(partnerVehicle.getRoute());

            // TODO: Parameter optimize
            List<Vehicle> child1Vehicles = parents[0].fitnessCrossOver(routesFromS2[0]);
            List<Vehicle> child2Vehicles = parents[0].fitnessCrossOver(routesFromS2[1]);
            List<Vehicle> child3Vehicles = parents[1].fitnessCrossOver(routesFromS1[0]);
            List<Vehicle> child4Vehicles = parents[1].fitnessCrossOver(routesFromS1[1]);

                        /*
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
             */

            if (child1Vehicles != null || child2Vehicles != null) {
                Individual child1 = new Individual(depots, durationPenaltyRate, loadPenaltyRate, child1Vehicles);
                Individual child2 = new Individual(depots, durationPenaltyRate, loadPenaltyRate, child2Vehicles);
                Individual child3 = new Individual(depots, durationPenaltyRate, loadPenaltyRate, child3Vehicles);
                Individual child4 = new Individual(depots, durationPenaltyRate, loadPenaltyRate, child4Vehicles);
                return new Individual[]{child1, child2, child3, child4};
            } else {
                triesLeft--;
            }
        }
        return null;
    }

    private Individual[] selection() {
        // TODO: Optimize parameters
//        Individual parent1 = tournament();
        Individual parent1 = rouletteWheel();
        Individual parent2 = parent1;
        while (parent1 == parent2) {
//            parent2 = tournament();
            parent2 = rouletteWheel();
        }

        return new Individual[]{parent1, parent2};
    }

    private Individual rouletteWheel() {
        double totalFitness = 0.0;

        for (Individual individual : individuals) {
            totalFitness += individual.calculateFitness();
        }

        int threshold = Utils.randomIndex((int) totalFitness);
        totalFitness = 0.0;

        for (Individual individual : individuals) {
            totalFitness += individual.calculateFitness();

            if ((int) totalFitness > threshold) {
                return individual;
            }
        }

        return null;
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
        tournamentMembers.sort(Comparator.comparingDouble(Individual::calculateFitness));
        return tournamentMembers.get(0);
    }

    public double getAlphaDuration() {
        return alphaIndividual.getDuration();
    }

    public double getAlphaFitness() {
        return alphaIndividual.calculateFitness();
    }

    public Individual getAlphaIndividual() {
        if (individuals.size() == 0) {
            return null;
        }

        individuals.sort(Comparator.comparingDouble(Individual::calculateFitness)); // Sorts based on fitness
        alphaIndividual = individuals.get(0); // Best Individual
        return alphaIndividual;
    }

    public double getAverageFitness() {
        double fitness = 0.0;
        for (Individual s : individuals) {
            fitness += s.getFitness();
        }
        return fitness / individuals.size();
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
