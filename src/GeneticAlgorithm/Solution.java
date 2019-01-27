package GeneticAlgorithm;

import MapObjects.Vehicle;
import Utils.Utils;
import MapObjects.Customer;
import MapObjects.Depot;

import java.util.ArrayList;
import java.util.List;

public class Solution {
    private List<Depot> depots;
    private List<Vehicle> vehicles = new ArrayList<>();

    private int fitness;
    private double totalDistance;
    private double crossOverRate;
    private double mutationRate;

    public Solution(List<Depot> depots, double crossOverRate, double mutationRate) {
        this.depots = depots;
        this.crossOverRate = crossOverRate;
        this.mutationRate = mutationRate;
        generateInitialSolution();
        calculateTotalDistance();
        calculateFitness();
    }

    public void tick() {
        System.out.println("Vehicles size before crossover: " + vehicles.size());
        vehicles = crossOver(vehicles);
        System.out.println("Vehicles size before mutation: " + vehicles.size());
        vehicles = mutation(vehicles);
        System.out.println("Vehicles size before selection: " + vehicles.size());
        calculateFitness();
    }

    /**
     * Spreads the customers of a depot to random vehicles. Shuffles the genes in each chromosome of the vehicles.
     */
    public void generateInitialSolution() {
        System.out.println("========= Creating random initial vehicles =========");
        for (Depot depot : this.depots) {
            List<Vehicle> depotVehicles = depot.getVehicles();
            List<Customer> depotCustomers = depot.getCustomers();

            for (Customer customer : depotCustomers) {
                int randomIndex = Utils.randomIndex(depotVehicles.size() - 1);
                depotVehicles.get(randomIndex).addCustomer(customer);
            }

            for (Vehicle vehicle : vehicles) {
                vehicle.optimizeRoute();
            }

            this.vehicles.addAll(depotVehicles);

            System.out.println("========= END Creating random initial vehicles =========");
        }
    }

    /**
     * Performs crossOver on the vehicles
     * TODO: Test crossOver only between Vehicles from same Depot
     *
     * @param vehicles
     */
    public List<Vehicle> crossOver(List<Vehicle> vehicles) {
        System.out.println("========= Performing crossover on vehicles =========");

        List<Vehicle> newVehicles = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            double random = Utils.randomDouble();
            if (random > crossOverRate) {
                Vehicle otherVehicle = getCrossOverPartner(vehicle);

                List<Customer> newRoute = vehicle.crossOver(otherVehicle.getRoute(), 1);
                Vehicle newVehicle = new Vehicle(vehicle.getDepot(), newRoute);
                newVehicles.add(newVehicle);
            }
        }

        System.out.println("========= END Performing crossover on vehicles =========");

        return newVehicles;
    }

    /**
     * Performs n random mutations on the vehicles based on the mutationRate
     *
     * @param vehicles
     */
    public List<Vehicle> mutation(List<Vehicle> vehicles) {
        System.out.println("========= Performing mutation on vehicles =========");
        List<Vehicle> newVehicles = new ArrayList<>();

        //TODO: One or more mutations can happen on the same route, is this good or should it be max one mutation per route?
        for (Vehicle vehicle : vehicles) {
            double random = Utils.randomDouble();
            if (random > mutationRate) {
                List<Customer> newRoute = vehicle.mutate();
                Vehicle newVehicle = new Vehicle(vehicle.getDepot(), newRoute);
                newVehicles.add(newVehicle);
            }
        }

        vehicles.addAll(newVehicles);

        System.out.println("========= END Performing mutation on vehicles =========");
        return vehicles;
    }

    private Vehicle getCrossOverPartner(Vehicle vehicle) {
        Vehicle partner = vehicle;
        List<Vehicle> possiblePartners = vehicle.getDepot().getVehicles();

        while (vehicle == partner) {
            partner = possiblePartners.get(Utils.randomIndex(possiblePartners.size() - 1));
        }

        return partner;
    }


    public int getFitness() {
        return fitness;
    }

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public void calculateFitness() {
        int fitness = 0;

        for (Vehicle vehicle : vehicles) {
            fitness -= (int) vehicle.calculateRouteDistance();
        }

        this.fitness = fitness;
    }

    public void calculateTotalDistance() {
        int totalDistance = 0;

        for (Vehicle vehicle : vehicles) {
            totalDistance += (int) vehicle.calculateRouteDistance();
        }

        this.totalDistance = totalDistance;
    }
}
