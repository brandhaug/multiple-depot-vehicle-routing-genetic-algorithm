package GeneticAlgorithm;

import Main.Utils;
import MapObjects.Customer;
import MapObjects.Vehicle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

//TODO: Spawn method?
public class GeneticAlgorithm {

    private double mutationRate = 0.1;
    private List<Vehicle> population;
    private List<Vehicle> newPopulation;


    public GeneticAlgorithm(List<Vehicle> population) {
        this.population = population;
        tick(population);
    }

    public void tick(List<Vehicle> population) {
        List<Vehicle> newPopulation = new ArrayList<>();
        population = crossOver(population);
        population = mutation(population);
        population = selection(population, 0);
        this.newPopulation = population;
    }

    /**
     * Selects the chromosome with the lowest distance
     *
     * @param sortedPopulation
     * @return
     */
    public List<Customer> getAlphaChromosome(List<Vehicle> sortedPopulation) {
        return sortedPopulation.get(0).getChromosome();

    }

    /**
     * Performs crossOver on the population
     * TODO: Test crossOver only between Vehicles from same Depot
     * @param population
     */
    public List<Vehicle> crossOver(List<Vehicle> population) {
        List<Vehicle> newPopulation = new ArrayList<>();

        for (Vehicle vehicle : this.population) {
            Vehicle otherVehicle = getCrossOverPartner(vehicle, this.population);


            List<Customer> newChromosome = vehicle.crossOver(otherVehicle.getChromosome(), 1);
            Vehicle newVehicle = new Vehicle(vehicle.getDepot(), newChromosome);
            newPopulation.add(newVehicle);
        }

        return newPopulation;
    }

    /**
     * Performs n random mutations on the population based on the mutationRate
     *
     * @param population
     */
    public List<Vehicle> mutation(List<Vehicle> population) {
        List<Vehicle> newPopulation = new ArrayList<>();

        //TODO: One or more mutations can happen on the same chromosome, is this good or should it be max one mutation per chromosome?
        for (int i = 0; i < this.population.size() * mutationRate; i++) {
            Vehicle vehicle = this.population.get(Utils.randomIndex(this.population.size()));
            List<Customer> newChromosome = vehicle.mutate();
            Vehicle newVehicle = new Vehicle(vehicle.getDepot(), newChromosome);
            newPopulation.add(newVehicle);
        }

        return newPopulation;
    }

    /**
     * Sorts the population based on chromosome distances and selects n best chromosomes
     *
     * @param population
     */
    public List<Vehicle> selection(List<Vehicle> population, int limit) {
        population.sort(Comparator.comparingDouble(Vehicle::calculateChoromosomeDistance));
        population = population.stream().limit(this.population.size()).collect(Collectors.toList()); //      TODO: Change limit
        return population;
    }

    private Vehicle getCrossOverPartner(Vehicle vehicle, List<Vehicle> population) {
        Vehicle partner = vehicle;

        while (vehicle == partner) {
            partner = population.get(Utils.randomIndex(population.size()));
        }

        return partner;
    }

    /**
     * Shuffles the genes in each chromosome of the population
     */
    public void generateInitialPopulation() {
        for (Vehicle vehicle: this.population) {
            vehicle.generateInitialChromosome();
        }
    }
}
