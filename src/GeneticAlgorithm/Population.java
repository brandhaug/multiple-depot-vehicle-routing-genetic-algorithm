package GeneticAlgorithm;

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
    private List<Double> generations = new ArrayList<>();

    private int populationSize; // Number of Solutions in population
    private double crossOverRate;
    private double mutationRate;
    private int tournamentSize;
    private double selectionRate;
    private int numberOfChildren;
    private int numberOfParentsToSave;
    private int durationPenaltyRate;
    private int loadPenaltyRate;
    private boolean elitism;
    private int k;

    /**
     * Sets parameters
     * Generates initial population which generates n random Solutions. n = populationSize
     */
    public Population(List<Depot> depots,
                      int populationSize,
                      double crossOverRate,
                      double mutationRate,
                      int tournamentSize,
                      int numberOfChildren,
                      int numberOfParentsToSave,
                      int durationPenaltyRate,
                      int loadPenaltyRate,
                      boolean elitism,
                      int k) {
        this.depots = depots;
        this.populationSize = populationSize;
        this.crossOverRate = crossOverRate;
        this.mutationRate = mutationRate;
        this.selectionRate = selectionRate;
        this.tournamentSize = tournamentSize;
        this.numberOfChildren = numberOfChildren;
        this.numberOfParentsToSave = numberOfParentsToSave;
        this.durationPenaltyRate = durationPenaltyRate;
        this.loadPenaltyRate = loadPenaltyRate;
        this.elitism = elitism;
        this.k = k;
    }

    /**
     * One generation of Population
     * Loops through one generation of each Individual
     * 1. Selection
     * 2. Crossover
     * 3. Mutation
     * 4. Calculate fitness
     * 5. Filtering
     */
    public void tick() {
        if (generation == 0) {
            generateInitialPopulation();
            individuals.sort(Comparator.comparingDouble(Individual::getFitness));
        } else {
            List<Individual> children = new ArrayList<>();
            List<Individual> parentsToRemove = new ArrayList<>();
            for (int i = 0; i < numberOfChildren; i++) { // Would this actually make 2*numberOfChildren?
                Individual[] parents = selection();

                // Parents get to crossover if random is less than crossOverRate
                double random = Utils.randomDouble();
                if (random < crossOverRate) {
                    children.addAll(crossOver(parents));
                }

                if (!elitism && children.size() != 0) {
                    parentsToRemove.addAll(List.of(parents[0], parents[1]));
                }
            }

            List<Individual> childrenToAdd = new ArrayList<>();
            for (Individual child : children) {
                double random = Utils.randomDouble();
                if (random < mutationRate) {
                    // TODO: Optimize parameters
//                    Individual mutatedChild = new Individual(depots, durationPenaltyRate, loadPenaltyRate, child.swapMutation());
                    Individual mutatedChild = new Individual(depots, durationPenaltyRate, loadPenaltyRate, child.swapMutation2());
//                    Individual mutatedChild = new Individual(depots, durationPenaltyRate, loadPenaltyRate, child.crossMutation());
                    childrenToAdd.add(mutatedChild);
                } else {
                    childrenToAdd.add(child);
                }
            }

            if (!elitism) {
                individuals.removeAll(parentsToRemove);
            }

            individuals.sort(Comparator.comparingDouble(Individual::getFitness)); // Sort by fitness
            List<Individual> parentsToSave = new ArrayList<>(individuals.subList(0, numberOfParentsToSave));
            individuals = childrenToAdd;
            individuals.addAll(parentsToSave);
            individuals.sort(Comparator.comparingDouble(Individual::getFitness)); // Sort by fitness
            individuals = individuals.stream().limit(populationSize).collect(Collectors.toList()); // Cut population to population size
        }
        alphaIndividual = individuals.get(0);
        generations.add(getAlphaFitness());
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
            boolean successful = individual.generateOptimizedIndividual2(force);
//            boolean successful = individual.generateRandomIndividual();

            if (successful) {
                individual.calculateFitness();
                individuals.add(individual);
            } else {
                triesLeft--;
            }
        }

        if (triesLeft == 0) {
//            throw new Error("Generating initial population failed - created " + individuals.size() + " of " + populationSize + " individuals");
            System.out.println("Generated population with constraint break");
        }
    }

    private List<Individual> crossOver(Individual[] parents) {
        List<Vehicle> solutionVehicles = parents[0].getVehicles();
        int randIndex = Utils.randomIndex(solutionVehicles.size());
        Vehicle solutionVehicle = solutionVehicles.get(randIndex);

        List<Vehicle> partnerVehicles = parents[1].getVehicles();
        int randIndex2 = Utils.randomIndex(parents[1].getVehicles().size());

        Vehicle partnerVehicle = partnerVehicles.get(randIndex2);
        ArrayList<ArrayList<Customer>> partsFromS1 = Utils.splitRoute(solutionVehicle.getRoute(), k);
        ArrayList<ArrayList<Customer>> partsFromS2 = Utils.splitRoute(partnerVehicle.getRoute(), k);

        List<Individual> children = new ArrayList<>();

        for (int i = 0; i < k; i++) {
            List<Vehicle> newVehicles = parents[0].singlePointCrossOver(partsFromS2.get(i));
            List<Vehicle> newVehicles2 = parents[1].singlePointCrossOver(partsFromS1.get(i));
            children.add(new Individual(depots, durationPenaltyRate, loadPenaltyRate, newVehicles));
            children.add(new Individual(depots, durationPenaltyRate, loadPenaltyRate, newVehicles2));
        }

        return children;
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
        double totalFitness = getTotalFitness();

        int threshold = Utils.randomIndex((int) totalFitness);
        totalFitness = 0.0;

        for (Individual individual : individuals) {
            totalFitness += individual.getFitness();

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
                int randomIndex = Utils.randomIndex(individuals.size());
                member = individuals.get(randomIndex);
                contained = tournamentMembers.contains(member);
            }
            tournamentMembers.add(member);
        }
        tournamentMembers.sort(Comparator.comparingDouble(Individual::getFitness));
        return tournamentMembers.get(0);
    }

    private double getTotalFitness() {
        double totalFitness = 0.0;

        for (Individual individual : individuals) {
            totalFitness += individual.getFitness();
        }

        return totalFitness;
    }

    public double getAlphaDuration() {
        return alphaIndividual.getDuration();
    }

    public double getAlphaFitness() {
        return alphaIndividual.getFitness();
    }

    public Individual getAlphaIndividual() {
        return alphaIndividual;
    }

    public double getAverageFitness() {
        double totalFitness = getTotalFitness();
        return totalFitness / individuals.size();
    }

    public int getGeneration() {
        return generation;
    }

    public boolean isAlphaValid() {
        return alphaIndividual.isValid();
    }

    public List<Double> getGenerations() {
        return generations;
    }
}
