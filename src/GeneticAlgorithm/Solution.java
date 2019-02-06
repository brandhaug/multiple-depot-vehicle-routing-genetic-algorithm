package GeneticAlgorithm;

import Main.Controller;
import MapObjects.Vehicle;
import Utils.Utils;
import MapObjects.Customer;
import MapObjects.Depot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One Solution in Population
 * Contains Routes from all Vehicles
 */
public class Solution {
    private List<Depot> depots;
    private List<Vehicle> vehicles;

    /**
     * Generates initialSolution and calculates distances
     *
     * @param depots
     */
    public Solution(List<Depot> depots) {
        this.depots = depots;
        vehicles = new ArrayList<>();
    }

    public Solution(List<Depot> depots, List<Vehicle> vehicles) {
        this.depots = depots;
        this.vehicles = new ArrayList<>(vehicles);
    }

    /**
     * Each Depot has n Vehicles and m Customers
     * Loops through all Depots, and assigns the Depot's Customers to a random Depot's vehicle.
     */
    public boolean generateInitialSolution() {
        if (Controller.verbose) {
            System.out.println("========= Creating random initial vehicles =========");
        }

        for (Depot depot : depots) {
            List<Vehicle> depotVehicles = new ArrayList<>();

            for (int i = 0; i < depot.getMaxCars(); i++) {
                Vehicle v = new Vehicle(depot);
                depotVehicles.add(v);
            }

            List<Customer> depotCustomers = new ArrayList<>(depot.getCustomers()); // Current depot's customers
            Collections.shuffle(depotCustomers);

            for (Customer customer : depotCustomers) {
                boolean customerAdded = false;
                int triesLeft = 100;
                while (!customerAdded && triesLeft > 0) {

                    double currentMinDistance = Double.MAX_VALUE;
                    int currentMinIndex = 0;
                    Vehicle currentMinVehicle = null;

                    Collections.shuffle(depotVehicles);
                    for (Vehicle vehicle : depotVehicles) {
                        if (vehicle.getCurrentLoad() + customer.getLoadDemand() <= depot.getMaxLoad()) {
                            double distance;
                            if (vehicle.getRoute().size() == 0) {
                                currentMinDistance = 0.0;
                                currentMinIndex = 0;
                                currentMinVehicle = vehicle;
                            }

                            for (int i = 0; i < vehicle.getRoute().size(); i++) {
                                distance = vehicle.calculateRouteDuration(i, customer);

                                if (distance < currentMinDistance) {
                                    currentMinDistance = distance;
                                    currentMinIndex = i;
                                    currentMinVehicle = vehicle;
                                }
                            }
                        }
                    }
                    if (currentMinVehicle != null) {
                        currentMinVehicle.addCustomerToRoute(currentMinIndex, customer);

                        if (depot.getMaxDuration() != 0.0 && currentMinVehicle.calculateRouteDuration() > depot.getMaxDuration()) {
                            currentMinVehicle.removeCustomerFromRoute(customer);
                            triesLeft--;
                        }
                        else {
                            customerAdded = true;
                        }
                    }
                    else {
                        triesLeft--;
                    }
                }
                if (triesLeft == 0) { // Giving up generating this initial solution
                    return false;
                }
            }
            this.vehicles.addAll(depotVehicles);
        }


        if (Controller.verbose) {
            System.out.println("========= END Creating random initial vehicles =========");
        }

        return true;
    }

    public List<Vehicle> mutation2(List<Vehicle> vehicles) {
        System.out.println("========= Performing crossover on vehicles =========");

        List<Vehicle> newVehicles = new ArrayList<>(vehicles);

        int randomIndex = Utils.randomIndex(vehicles.size());
        Vehicle vehicle = vehicles.get(randomIndex);
        Vehicle otherVehicle = getMutationPartner(vehicle);

        List<Customer>[] newRoutes = vehicle.mutation2(otherVehicle.getRoute());
        Vehicle newVehicle = new Vehicle(vehicle.getStartDepot(), newRoutes[0]);
        Vehicle newVehicle2 = new Vehicle(vehicle.getStartDepot(), newRoutes[1]);

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
     *
     * @param vehicle
     * @return
     */
    private Vehicle getMutationPartner(Vehicle vehicle) {
        Vehicle partner = vehicle;
        List<Vehicle> possiblePartners = new ArrayList<>();

        for (Vehicle v : vehicles) {
            if (v.getStartDepot() == vehicle.getStartDepot()) {
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
        List<Customer> newRoute = vehicle.mutate();
        // TODO: vehicle.clone() - mest sannsynlig ikke nødvendig
        Vehicle newVehicle = new Vehicle(vehicle.getStartDepot(), newRoute);

        newVehicles.remove(vehicle);
        newVehicles.add(newVehicle);

        System.out.println("========= END Performing mutation on vehicles =========");
        return newVehicles;
    }

    /**
     * Calculates fitness for all routes in solution
     */
    public double getFitness() {
        double fitness = 0.0;

        for (Vehicle vehicle : vehicles) {
            fitness += vehicle.calculateRouteDistance();
        }

        return fitness;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public List<Vehicle> crossOver(List<Customer> otherRoute) {
        if (Controller.verbose) {
            System.out.println("Performing mutation2");
        }

        if (vehicles == null) {
            throw new NullPointerException("No vehicles in solution");
        } else if (otherRoute.size() == 0) {
            System.out.println("Other Route is 0");
            return vehicles;
        }

        // Creating a deep copy of vehicles
        List<Vehicle> newVehicles = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            newVehicles.add(vehicle.clone());
        }

        // Remove route from routeFromPartner
        for (Vehicle vehicle : newVehicles) {
            List<Customer> routeCopy = new ArrayList<>(vehicle.getRoute()); // Copy to avoid error when removing while looping
            for (Customer customer : routeCopy) {
                if (otherRoute.contains(customer)) {
                    vehicle.removeCustomerFromRoute(customer);
                }
            }
        }

        // Rull gjennom alle ruter og regn ut diff i fitness på alle mulige steder
        double minDistance = Double.MAX_VALUE;
        Vehicle minVehicle = null;
        int minIndex = -1;

        for (Vehicle vehicle : newVehicles) {
            for (int i = 0; i < vehicle.getRoute().size(); i++) {
                Customer customer = vehicle.getRoute().get(i);

                double distance = 0.0;

                if (i == 0) { // Check between depot and first customer
                    distance += Utils.euclideanDistance(otherRoute.get(0).getX(), vehicle.getStartDepot().getX(), otherRoute.get(0).getY(), vehicle.getStartDepot().getY());
                    distance += Utils.euclideanDistance(otherRoute.get(otherRoute.size() - 1).getX(), customer.getX(), otherRoute.get(otherRoute.size() - 1).getY(), customer.getY());
                } else if (i == newVehicles.size() - 1) { // Check between last customer and depot
                    distance += Utils.euclideanDistance(customer.getX(), otherRoute.get(0).getX(), customer.getY(), otherRoute.get(0).getY());
                    distance += Utils.euclideanDistance(vehicle.getStartDepot().getX(), otherRoute.get(otherRoute.size() - 1).getX(), vehicle.getStartDepot().getY(), otherRoute.get(otherRoute.size() - 1).getY());
                } else { // Check between customers
                    Customer lastCustomer = vehicle.getRoute().get(i - 1);
                    distance += Utils.euclideanDistance(otherRoute.get(0).getX(), lastCustomer.getX(), otherRoute.get(0).getY(), lastCustomer.getY());
                    distance += Utils.euclideanDistance(otherRoute.get(otherRoute.size() - 1).getX(), customer.getX(), otherRoute.get(otherRoute.size() - 1).getY(), customer.getY());
                }

                if (distance < minDistance) {
                    int loadIfAdded = vehicle.getCurrentLoad();
                    for (Customer c : otherRoute) {
                        loadIfAdded += c.getLoadDemand();
                    }

                    // Checking constraints
                    if (loadIfAdded <= vehicle.getStartDepot().getMaxLoad()) {
                        minDistance = distance;
                        minVehicle = vehicle;
                        minIndex = i;
                    }
                }
            }

            if (minVehicle == null) {
                return vehicles;
            }

            // Find best ending point for route
            if (vehicle.getRoute().size() > 0) {
                double currentMinDistance = Double.MAX_VALUE;
                Depot currentBestEnd = vehicle.getStartDepot();
                for (Depot d : depots) {
                    double distance = Utils.euclideanDistance(vehicle.getRoute().get(vehicle.getRoute().size() - 1).getX(), d.getX(),
                            vehicle.getRoute().get(vehicle.getRoute().size() - 1).getY(), d.getY());
                    if (distance < currentMinDistance) {
                        currentMinDistance = distance;
                    }
                }
                vehicle.setEndDepot(currentBestEnd);
            }
        }

//        if (minDistance == Double.MAX_VALUE) return null;

        // Legg inn routeFromOtherSolution der fitness er best
        // Legge inn end depot her?
        // TODO: Error skjer når alle routes sizes i newVehicles er 0 (line 257)
        minVehicle.addOtherRouteToRoute(minIndex, otherRoute);

        System.out.println("CrossOver finished, returning new list of Vehicles");

        return newVehicles;
    }

//    @Override
//    public Solution clone() {
//        List<Vehicle> copyOfVehicles = new ArrayList<>();
//        for (Vehicle vehicle : vehicles) {
//            copyOfVehicles.add(vehicle.clone());
//        }
//        return new Solution(depots, copyOfVehicles);
//    }
}
