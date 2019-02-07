package GeneticAlgorithm;

import Main.Controller;
import MapObjects.Vehicle;
import Utils.Utils;
import MapObjects.Customer;
import MapObjects.Depot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One Individual in Population
 * Contains Routes from all Vehicles
 */
public class Individual {
    private List<Depot> depots;
    private List<Vehicle> vehicles;
    private boolean isValid;

    /**
     * Generates initialSolution and calculates distances
     *
     * @param depots
     */
    public Individual(List<Depot> depots) {
        this.depots = depots;
        vehicles = new ArrayList<>();
    }

    public Individual(List<Depot> depots, List<Vehicle> vehicles) {
        this.depots = depots;
        this.vehicles = new ArrayList<>(vehicles);
    }

    /**
     * Each Depot has n Vehicles and m Customers
     * Loops through all Depots, and assigns the Depot's Customers to a random Depot's vehicle.
     * @param force
     */
    public boolean generateInitialSolution(boolean force) {
        if (Controller.verbose) {
            System.out.println("========= Creating random initial vehicles =========");
        }

        for (Depot depot : depots) {
            List <Vehicle> depotVehicles = createDepotVehicles(depot);

            List<Customer> depotCustomers = new ArrayList<>(depot.getCustomers()); // Current depot's customers
            Collections.shuffle(depotCustomers);

            for (Customer customer : depotCustomers) {
                boolean customerAdded = false;
                int triesLeft = 100;
                while (!customerAdded && triesLeft > 0) {

                    double currentMinDistance = Double.MAX_VALUE;
                    int currentMinIndex = -1;
                    Vehicle currentMinVehicle = null;

                    Collections.shuffle(depotVehicles);
                    for (Vehicle vehicle : depotVehicles) {

                        double distance;
                        double vehicleMinDistance = Double.MAX_VALUE;
                        int vehicleMinIndex = -1;


                        if (vehicle.getCurrentLoad() + customer.getLoadDemand() <= depot.getMaxLoad()) {
                            if (vehicle.getRoute().size() == 0) {
                                vehicleMinDistance = vehicle.calculateRouteDuration(0, customer);
                                vehicleMinIndex = 0;
                            } else {
                                for (int i = 0; i < vehicle.getRoute().size(); i++) {
                                    distance = vehicle.calculateRouteDuration(i, customer);

                                    if (distance < vehicleMinDistance) {
                                        vehicleMinDistance = distance;
                                        vehicleMinIndex = i;
                                    }
                                }
                            }
                        }

                        if (vehicleMinDistance < currentMinDistance) {
                            currentMinDistance = vehicleMinDistance;
                            currentMinIndex = vehicleMinIndex;
                            currentMinVehicle = vehicle;
                        }
                    }
                    if (currentMinVehicle != null) {
                        currentMinVehicle.addCustomerToRoute(currentMinIndex, customer);

                        if (depot.getMaxDuration() != 0.0 && currentMinVehicle.calculateRouteDuration() > depot.getMaxDuration() && !force) {
                            currentMinVehicle.removeCustomerFromRoute(customer);
                            triesLeft--;
                        } else {
                            customerAdded = true;
                        }
                    } else {
                        triesLeft--;
                    }
                }
                if (triesLeft == 0 && !force) { // Giving up generating this initial isValid
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

    /**
     * Each Depot has n Vehicles and m Customers
     * Loops through all Depots, and assigns the Depot's Customers to a random Depot's vehicle.
     * @param force
     */
    public boolean generateInitialSolution2(boolean force) {
        if (Controller.verbose) {
            System.out.println("========= Creating random initial vehicles =========");
        }

        for (Depot depot : depots) {
            List<Vehicle> depotVehicles = createDepotVehicles(depot);
            List<Customer> depotCustomers = depot.getCustomers(); // Current depot's customers
            Collections.shuffle(depotCustomers);

            for (Customer customer : depotCustomers) { // Assign customer to random vehicle
                boolean customerAdded = false;
//                int customerTriesLeft = 100;
                while (!customerAdded) {
                    int randomIndex = Utils.randomIndex(depotVehicles.size()); // Random vehicle index
                    Vehicle randomVehicle = depotVehicles.get(randomIndex);

                    // Check load constraint
                    if (randomVehicle.getCurrentLoad() + customer.getLoadDemand() <= depot.getMaxLoad()) {
                        customerAdded = randomVehicle.addCustomerToRouteSmart(customer);

//                        // Check duration constraint
//                        if (depot.getMaxDuration() != 0.0) {
//                            randomVehicle.optimizeRoute();
//
//                            if (randomVehicle.calculateRouteDuration() > depot.getMaxDuration() && !force) {
//                                randomVehicle.removeCustomerFromRoute(customer);
//                                customerTriesLeft--;
//                            } else  {
//                                customerAdded = true;
//                            }
//                        } else {
//                            customerAdded = true;
//                        }
                    }
                }
//                if (customerTriesLeft == 0) { // Giving up generating this initial isValid
//                    return false;
//                }
            }

            this.vehicles.addAll(depotVehicles);

            if (Controller.verbose) {
                System.out.println("========= END Creating random initial vehicles =========");
            }
        }

        return true;
    }

    private List<Vehicle> createDepotVehicles(Depot depot) {
        List<Vehicle> vehicles = new ArrayList<>();

        for (int i = 0; i < depot.getMaxVehicles(); i++) {
            Vehicle v = new Vehicle(depot);
            vehicles.add(v);
        }
        return vehicles;
    }

    public List<Vehicle> mutation2() {
        if (Controller.verbose) {
            System.out.println("========= Performing mutation on isValid =========");
        }

        // Copy of vehicles
        List<Vehicle> newVehicles = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            newVehicles.add(vehicle.clone());
        }

        // Pick two random vehicles
        int randomIndex1 = Utils.randomIndex(vehicles.size());
        int randomIndex2 = Utils.randomIndex(vehicles.size());
        while (randomIndex1 == randomIndex2) {
            randomIndex2 = Utils.randomIndex(vehicles.size());
        }
        Vehicle randomVehicle1 = vehicles.get(randomIndex1).clone();
        Vehicle randomVehicle2 = vehicles.get(randomIndex2).clone();

        // Remove the old vehicles
        newVehicles.remove(randomVehicle1);
        newVehicles.remove(randomVehicle2);

        // TODO: CHECK CONSTRAINTS, AND ALSO MAKE SURE LOAD IS CORRECT
        // Mutate the two vehicles and add them to newVehicles
        List<Customer>[] newRoutes = randomVehicle1.mutation2(randomVehicle2.getRoute());
        newVehicles.add(new Vehicle(randomVehicle1.getStartDepot(), randomVehicle1.getEndDepot(), newRoutes[0]));
        newVehicles.add(new Vehicle(randomVehicle2.getStartDepot(), randomVehicle2.getEndDepot(), newRoutes[1]));

        if (Controller.verbose) {
            System.out.println("========= END Performing mutation on isValid =========");
        }

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
        if (Controller.verbose) {
            System.out.println("========= Performing mutation on vehicles =========");
        }
        List<Vehicle> newVehicles = new ArrayList<>(vehicles);

        int randomIndex = Utils.randomIndex(vehicles.size());
        Vehicle vehicle = vehicles.get(randomIndex);
        List<Customer> newRoute = vehicle.mutate();
        Vehicle newVehicle = new Vehicle(vehicle.getStartDepot(), newRoute);

        newVehicles.remove(vehicle);
        newVehicles.add(newVehicle);

        if (Controller.verbose) {
            System.out.println("========= END Performing mutation on vehicles =========");
        }
        return newVehicles;
    }

    /**
     * Calculates fitness for all routes in isValid
     */
    public double getFitness() {
        double fitness = 0.0;
        isValid = true;

        for (Vehicle vehicle : vehicles) {
            double penalty = 0;

            double maxDuration = vehicle.getStartDepot().getMaxDuration();
            double duration = vehicle.calculateRouteDuration();
            if (maxDuration != 0 && duration > maxDuration) {
                penalty += Math.pow((duration - maxDuration), 2);
            }

            double maxLoad = vehicle.getStartDepot().getMaxLoad();
            double load = vehicle.getCurrentLoad();
            if (maxLoad != 0 && load > maxLoad) {
                penalty += Math.pow((load - maxLoad), 2);
            }

            if (penalty > 0) {
                isValid = false;
            }

            fitness += (duration + penalty);
        }

        return fitness;
    }

    public double getDuration() {
        double duration = 0.0;

        for (Vehicle vehicle : vehicles) {
            duration += vehicle.calculateRouteDuration();
        }

        return duration;
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
            if (Controller.verbose) {
                System.out.println("Other Route is 0");
            }
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
            if (vehicle.getRoute().size() == 0) {
                double distance = 0.0;
                distance += Utils.euclideanDistance(otherRoute.get(0).getX(), vehicle.getStartDepot().getX(), otherRoute.get(0).getY(), vehicle.getStartDepot().getY());
                distance += Utils.euclideanDistance(vehicle.getEndDepot().getX(), otherRoute.get(otherRoute.size() - 1).getX(), vehicle.getEndDepot().getY(), otherRoute.get(otherRoute.size() - 1).getY());

                if (distance < minDistance) {
                    int loadIfAdded = vehicle.getCurrentLoad();
                    for (Customer c : otherRoute) {
                        loadIfAdded += c.getLoadDemand();
                    }

                    // Checking constraints
                    if (loadIfAdded <= vehicle.getStartDepot().getMaxLoad()) {
                        minDistance = distance;
                        minVehicle = vehicle;
                        minIndex = 0;
                    }
                }
            } else {
                for (int i = 0; i < vehicle.getRoute().size(); i++) {
                    Customer customer = vehicle.getRoute().get(i);

                    double distance = 0.0;

                    if (i == 0) { // Check between depot and first customer
                        distance += Utils.euclideanDistance(otherRoute.get(0).getX(), vehicle.getStartDepot().getX(), otherRoute.get(0).getY(), vehicle.getStartDepot().getY());
                        distance += Utils.euclideanDistance(otherRoute.get(otherRoute.size() - 1).getX(), customer.getX(), otherRoute.get(otherRoute.size() - 1).getY(), customer.getY());
                    } else if (i == newVehicles.size() - 1) { // Check between last customer and depot
                        distance += Utils.euclideanDistance(customer.getX(), otherRoute.get(0).getX(), customer.getY(), otherRoute.get(0).getY());
                        distance += Utils.euclideanDistance(vehicle.getEndDepot().getX(), otherRoute.get(otherRoute.size() - 1).getX(), vehicle.getEndDepot().getY(), otherRoute.get(otherRoute.size() - 1).getY());
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
            }

            if (minVehicle == null) {
                return vehicles;
            }

            // Find best ending point for route
            if (vehicle.getRoute().size() > 0) {
                double currentMinDistance = Double.MAX_VALUE;
                Depot currentBestEnd = null;
                for (Depot d : depots) {
                    double distance = d.distance(vehicle.getRoute().get(vehicle.getRoute().size() - 1));
                    if (distance < currentMinDistance) {
                        currentMinDistance = distance;
                        currentBestEnd = d;
                    }
                }
                vehicle.setEndDepot(currentBestEnd);
            }
        }

        minVehicle.addOtherRouteToRoute(minIndex, otherRoute);

        if (Controller.verbose) {
            System.out.println("CrossOver finished, returning new list of Vehicles");
        }

        return newVehicles;
    }

    public boolean isValid() {
        return isValid;
    }

    public void saveToFile() throws IOException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        BufferedWriter writer = new BufferedWriter(new FileWriter("solution-" + Controller.fileName + "-" + timestamp.getTime()));

        writer.write(Double.toString(Utils.round(getDuration(), 2)));

        for (Vehicle vehicle : vehicles) {
            writer.newLine();
            writer.write(vehicle.getStartDepot().getId() + " " // s: number of the start depot
                    + vehicle.getStartDepot().getMaxVehicles() + " " // k: number of the vehicle (for above depot)
                    + Utils.round(vehicle.calculateRouteDuration(), 2) + " " // d: duration of the route for a particular vehicle from a particular depot
                    + vehicle.getCurrentLoad() + " " // q: carried load of the vehicle
                    + vehicle.getEndDepot().getId() + "   " // e: number of the end depot
                    + vehicle.getRoute().toString() // list: ordered sequence of customers (served by a particular vehicle)
                    .replace(",", "")  // remove the commas
                    .replace("[", "")  // remove the right bracket
                    .replace("]", "")  // remove the left bracket
            );
        }
        writer.close();
    }
}
