package GeneticAlgorithm;

import Main.Controller;
import MapObjects.Vehicle;
import Utils.Utils;
import MapObjects.Customer;
import MapObjects.Depot;

import java.util.ArrayList;
import java.util.List;

/**
 * One Solution in Population
 * Contains Routes from all Vehicles
 */
public class Solution {
    private List<Depot> depots;
    private List<Vehicle> vehicles = new ArrayList<>();

    private int fitness; // Same as totalDistance for now
    private double totalDistance; // Total distance of all Vehicle routes
    private double crossOverRate;
    private double mutationRate;

    /**
     * Generates initialSolution and calculates distances
     *
     * @param depots
     * @param crossOverRate
     * @param mutationRate
     */
    public Solution(List<Depot> depots, double crossOverRate, double mutationRate) {
        this.depots = depots;
        this.crossOverRate = crossOverRate;
        this.mutationRate = mutationRate;
        generateInitialSolution();
        calculateTotalDistance();
        calculateFitness();
    }

    /**
     * One generation of solution
     * 1. Crossover
     * 2. Mutation
     * 3. Calculate distance and fitness
     */
    public void tick() {
        if (Controller.verbose) {
            System.out.println("Vehicles size before crossover: " + vehicles.size());
        }

        vehicles = crossOver(vehicles); // 1. Crossover

        if (Controller.verbose) {
            System.out.println("Vehicles size before mutation: " + vehicles.size());
        }

        vehicles = mutation(vehicles); // 2. Mutation

        if (Controller.verbose) {
            System.out.println("Vehicles size before selection: " + vehicles.size());
        }

        // 3. Calculate distance and fitness
        calculateTotalDistance();
        calculateFitness();
    }

    /**
     * Each Depot has n Vehicles and m Customers
     * Loops through all Depots, and assigns the Depot's Customers to a random Depot's vehicle.
     */
    public void generateInitialSolution() {
        if (Controller.verbose) {
            System.out.println("========= Creating random initial vehicles =========");
        }

        for (Depot depot : this.depots) {
            List<Vehicle> depotVehicles = depot.getVehicles(); // Current depot's vehicles
            List<Customer> depotCustomers = depot.getCustomers(); // Current depot's customers


            for (Customer customer : depotCustomers) { // Assign customer to random vehicle
                int randomIndex = Utils.randomIndex(depotVehicles.size() - 1);
                depotVehicles.get(randomIndex).addCustomer(customer);
            }

            // Optimize route for each vehicle
            // TODO: Try vehicle.shuffle() to see difference
            for (Vehicle vehicle : vehicles) {
                vehicle.optimizeRoute();
            }


            this.vehicles.addAll(depotVehicles);

            if (Controller.verbose) {
                System.out.println("========= END Creating random initial vehicles =========");
            }
        }
    }

    /**
     * Performs crossOver on Vehicles in Solution
     * Crossover is only executed if random < crossOverRate
     * @param vehicles
     */
    public List<Vehicle> crossOver(List<Vehicle> vehicles) {
        System.out.println("========= Performing crossover on vehicles =========");

        List<Vehicle> newVehicles = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            double random = Utils.randomDouble();
            if (random < crossOverRate) {
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
     * Finds another Vehicle as partner for crossOver
     * Potential crossOverPartner can only be a Vehicle from same Depot
     * TODO: Try potential crossOverPartner with all vehicles
     * @param vehicle
     * @return
     */
    private Vehicle getCrossOverPartner(Vehicle vehicle) {
        Vehicle partner = vehicle;
        List<Vehicle> possiblePartners = vehicle.getDepot().getVehicles();

        while (vehicle == partner) {
            partner = possiblePartners.get(Utils.randomIndex(possiblePartners.size() - 1));
        }

        return partner;
    }

    /**
     * Performs n random mutations on the vehicles based on the mutationRate
     * Mutation is only executed if random < crossOverRate
     * @param vehicles
     */
    public List<Vehicle> mutation(List<Vehicle> vehicles) {
        System.out.println("========= Performing mutation on vehicles =========");
        List<Vehicle> newVehicles = new ArrayList<>();

        //TODO: One or more mutations can happen on the same route, is this good or should it be max one mutation per route?
        for (Vehicle vehicle : vehicles) {
            double random = Utils.randomDouble();
            if (random < mutationRate) {
                List<Customer> newRoute = vehicle.mutate();
                Vehicle newVehicle = new Vehicle(vehicle.getDepot(), newRoute);
                newVehicles.add(newVehicle);
            }
        }

        vehicles.addAll(newVehicles);

        System.out.println("========= END Performing mutation on vehicles =========");
        return vehicles;
    }

    /**
     * Calculates fitness for all routes in solution
     * TODO: Same as totalDistance: Either find a new fitness rating, or delete
     */
    public void calculateFitness() {
        int fitness = 0;

        for (Vehicle vehicle : vehicles) {
            fitness += (int) vehicle.calculateRouteDistance();
        }

        this.fitness = fitness;
    }

    /**
     * Calculates total distance for all routes in Solution.
     * TODO: Right now the distance are only calculated between customers, and not between depots and customers
     */
    public void calculateTotalDistance() {
        int totalDistance = 0;

        for (Vehicle vehicle : vehicles) {
            totalDistance += (int) vehicle.calculateRouteDistance();
        }

        this.totalDistance = totalDistance;
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

    public List<Vehicle> getVehicles() {
        return vehicles;
    }
}
