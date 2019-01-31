package GeneticAlgorithm;

import Main.Controller;
import MapObjects.Vehicle;
import Utils.Utils;
import MapObjects.Customer;
import MapObjects.Depot;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

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

    /**
     * Generates initialSolution and calculates distances
     *
     * @param depots
     */
    public Solution(List<Depot> depots) {
        this.depots = depots;
        generateInitialSolution();
    }

    public Solution(List<Depot> depots, List<Vehicle> vehicles) {
        this.depots = depots;
        this.vehicles = vehicles;
    }

    /**
     * One generation of solution
     * 1. Crossover
     * 2. Mutation
     * 3. Calculate distance and fitness
     */
    public void tick() {
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
            List<Vehicle> depotVehicles = new ArrayList<>();

            // Vehicle v is not used here, should it be a traditional for loop instead?
            for (Vehicle v : depot.getVehicles()) {
                Vehicle vCopy = new Vehicle(depot);
                depotVehicles.add(vCopy);
            }

            // TODO: Referanse??
            List<Customer> depotCustomers = depot.getCustomers(); // Current depot's customers

            // TODO: Ta høyde for vehicle sin max distance og max load
            for (Customer customer : depotCustomers) { // Assign customer to random vehicle
                int randomIndex = Utils.randomIndex(depotVehicles.size());
                depotVehicles.get(randomIndex).addCustomer(customer);
            }

            // Problem her med referanse - bør lage nye referanser
            this.vehicles.addAll(depotVehicles);

            if (Controller.verbose) {
                System.out.println("========= END Creating random initial vehicles =========");
            }
        }
        // Optimize route for each vehicle
        // TODO: Try vehicle.shuffle() to see difference
        for (Vehicle vehicle : vehicles) {
            vehicle.optimizeRoute();
        }
    }

    public List<Vehicle> mutation2(List<Vehicle> vehicles) {
        System.out.println("========= Performing crossover on vehicles =========");

        List<Vehicle> newVehicles = new ArrayList<>(vehicles);

        int randomIndex = Utils.randomIndex(vehicles.size());
        Vehicle vehicle = vehicles.get(randomIndex);
        Vehicle otherVehicle = getMutationPartner(vehicle);

        // TODO: Referanse??
        List<Customer>[] newRoutes = vehicle.crossOver(otherVehicle.getRoute(), 1);
        Vehicle newVehicle = new Vehicle(vehicle.getDepot(), newRoutes[0]);
        Vehicle newVehicle2 = new Vehicle(vehicle.getDepot(), newRoutes[1]);

        // Remove parents
        newVehicles.remove(vehicle);
        newVehicles.remove(otherVehicle);

        // Add children
        newVehicles.add(newVehicle);
        newVehicles.add(newVehicle2);


        System.out.println("========= END Performing crossover on vehicles =========");

        return newVehicles;
    }

    /**
     * Finds another Vehicle as partner for mutation
     * Potential mutationPartner can only be a Vehicle from same Depot
     * TODO: Try potential crossOverPartner with all vehicles
     *
     * @param vehicle
     * @return
     */
    private Vehicle getMutationPartner(Vehicle vehicle) {
        Vehicle partner = vehicle;
        List<Vehicle> possiblePartners = new ArrayList<>();

        for (Vehicle v : vehicles) {
            if (v.getDepot() == vehicle.getDepot()) {
                possiblePartners.add(v);
            }
        }

        while (vehicle == partner) {
            partner = possiblePartners.get(Utils.randomIndex(possiblePartners.size()));
        }

        return partner;
    }

    /**
     * Performs n random mutations on the vehicles based on the mutationRate
     * Mutation is only executed if random < crossOverRate
     */
    public List<Vehicle> mutation() {
        System.out.println("========= Performing mutation on vehicles =========");
        List<Vehicle> newVehicles = new ArrayList<>(vehicles);

        int randomIndex = Utils.randomIndex(vehicles.size());
        Vehicle vehicle = vehicles.get(randomIndex);
        // TODO: Referanse??
        List<Customer> newRoute = vehicle.mutate();
        Vehicle newVehicle = new Vehicle(vehicle.getDepot(), newRoute);

        newVehicles.remove(vehicle);
        newVehicles.add(newVehicle);

        System.out.println("========= END Performing mutation on vehicles =========");
        return newVehicles;
    }

    /**
     * Calculates fitness for all routes in solution
     * TODO: Same as totalDistance: Either find a new fitness rating, or delete
     */
    public int getFitness() {
        int fitness = 0;

        for (Vehicle vehicle : vehicles) {
            fitness += (int) vehicle.calculateRouteDistance();
        }

        return fitness;
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

    public List<Vehicle> crossOver(List<Customer> otherRoute) {
        if (Controller.verbose) {
            System.out.println("Performing crossOver");
        }

        // Creating a deep copy of vehicles
        List<Vehicle> newVehicles = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            newVehicles.add(vehicle.clone());
        }

        if (otherRoute.size() == 0) {
            System.out.println("Other Route is 0");
            return newVehicles;
        }

        // Remove route from routeFromPartner
        for (Vehicle vehicle : newVehicles) {
            List<Customer> routeCopy = new ArrayList<>(vehicle.getRoute()); // Copy to avoid error when removing while looping
            for (Customer customer : routeCopy) {
                if (otherRoute.contains(customer)) {
                    vehicle.getRoute().remove(customer);
                }
            }
        }

        // Rull gjennom alle ruter og regn ut diff i fitness på alle mulige steder
        double minDistance = Double.MAX_VALUE;
        Vehicle minVehicle = null;
        int minIndex = 0;

        for (Vehicle vehicle : newVehicles) {
            for (int i = 0; i < vehicle.getRoute().size(); i++) {
                Customer customer = vehicle.getRoute().get(i);

                double distance = 0.0;

                // Check between depot and first customer
                if (i == 0) {
                    distance += Utils.euclideanDistance(otherRoute.get(0).getX(), vehicle.getDepot().getX(), otherRoute.get(0).getY(), vehicle.getDepot().getY());
                    distance += Utils.euclideanDistance(otherRoute.get(otherRoute.size() - 1).getX(), customer.getX(), otherRoute.get(otherRoute.size() - 1).getY(), customer.getY());
                }
                // Check between last customer and depot
                else if (i == newVehicles.size() - 1) {
                    distance += Utils.euclideanDistance(customer.getX(), otherRoute.get(0).getX(), customer.getY(), otherRoute.get(0).getY());
                    distance += Utils.euclideanDistance(vehicle.getDepot().getX(), otherRoute.get(otherRoute.size() - 1).getX(), vehicle.getDepot().getY(), otherRoute.get(otherRoute.size() - 1).getY());
                } else {
                    Customer lastCustomer = vehicle.getRoute().get(i - 1);
                    distance += Utils.euclideanDistance(otherRoute.get(0).getX(), lastCustomer.getX(), otherRoute.get(0).getY(), lastCustomer.getY());
                    distance += Utils.euclideanDistance(otherRoute.get(otherRoute.size() - 1).getX(), customer.getX(), otherRoute.get(otherRoute.size() - 1).getY(), customer.getY());
                }

                if (distance < minDistance) {
                    minDistance = distance;
                    minVehicle = vehicle;
                    minIndex = i;
                }

            }
        }

        if (minVehicle == null) {
            System.out.println("WTF?"); // TODO: Error
        } else {
            // Legg inn routeFromOtherSolution der fitness er best
            minVehicle.getRoute().addAll(minIndex, otherRoute);
        }

        System.out.println("CrossOver finished, returning new list of Vehicles");

        return newVehicles;
    }

    @Override
    public Solution clone() {
        List<Vehicle> copyOfVehicles = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            copyOfVehicles.add(vehicle.clone());
        }
        return new Solution(depots, vehicles);
    }
}
