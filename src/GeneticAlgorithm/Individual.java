package GeneticAlgorithm;

import Main.Controller;
import MapObjects.Vehicle;
import Utils.Utils;
import MapObjects.Customer;
import MapObjects.Depot;

import java.io.BufferedWriter;
import java.io.File;
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
    private int durationPenaltyRate;
    private int loadPenaltyRate;
    private double fitness;
    private boolean isValid;

    /**
     * Generates initialSolution and calculates distances
     *
     * @param depots
     * @param durationPenaltyRate
     * @param loadPenaltyRate
     */
    public Individual(List<Depot> depots, int durationPenaltyRate, int loadPenaltyRate) {
        this.depots = depots;
        this.durationPenaltyRate = durationPenaltyRate;
        this.loadPenaltyRate = loadPenaltyRate;
        vehicles = new ArrayList<>();
        calculateFitness();
    }

    public Individual(List<Depot> depots, int durationPenaltyRate, int loadPenaltyRate, List<Vehicle> vehicles) {
        this.depots = depots;
        this.durationPenaltyRate = durationPenaltyRate;
        this.loadPenaltyRate = loadPenaltyRate;
        this.vehicles = new ArrayList<>(vehicles);
        calculateFitness();
    }

    public boolean generateOptimizedIndividual(boolean force) {
        for (Depot depot : depots) {
            List<Vehicle> depotVehicles = createDepotVehicles(depot);

            List<Customer> depotCustomers = new ArrayList<>(depot.getCustomers()); // Current depot's customers
            Collections.shuffle(depotCustomers);

            for (Customer customer : depotCustomers) {
                boolean customerAdded = false;
                int triesLeft = 100;
                while (!customerAdded && triesLeft > 0) {

                    double minDuration = Double.MAX_VALUE;
                    int minRouteIndex = -1;
                    Vehicle minVehicle = null;

                    Collections.shuffle(depotVehicles);
                    for (Vehicle vehicle : depotVehicles) {
                        double tempMinDuration = Double.MAX_VALUE;
                        int tempRouteIndex = -1;

                        if (vehicle.getCurrentLoad() + customer.getLoadDemand() <= depot.getMaxLoad()) {
                            if (vehicle.getRoute().size() == 0) {
                                tempMinDuration = vehicle.calculateRouteDurationIfCustomerAdded(0, customer);
                                tempRouteIndex = 0;
                            } else {
                                for (int i = 0; i < vehicle.getRoute().size(); i++) {
                                    double tempDuration = vehicle.calculateRouteDurationIfCustomerAdded(i, customer);

                                    if (tempDuration < tempMinDuration) {
                                        tempMinDuration = tempDuration;
                                        tempRouteIndex = i;
                                    }
                                }
                            }
                        }

                        if (tempMinDuration < minDuration) {
                            minDuration = tempMinDuration;
                            minRouteIndex = tempRouteIndex;
                            minVehicle = vehicle;
                        }
                    }

                    if (minVehicle == null) {
                        triesLeft--;
                    } else {
                        minVehicle.addCustomerToRoute(minRouteIndex, customer); // TODO: Error

                        if (depot.getMaxDuration() != 0.0 && minVehicle.calculateRouteDuration() > depot.getMaxDuration() && !force) {
                            minVehicle.removeCustomerFromRoute(customer);
                            triesLeft--;
                        } else {
                            customerAdded = true;
                        }
                    }
                }
                if (triesLeft == 0 && !force) { // Giving up generating this initial isValid
                    return false;
                }
            }
            this.vehicles.addAll(depotVehicles);
        }

        return true;
    }

    public boolean generateOptimizedIndividual2(boolean force) {
        for (Depot depot : depots) {
            List<Vehicle> depotVehicles = createDepotVehicles(depot);
            List<Customer> depotCustomers = depot.getCustomers(); // Current depot's customers
            Collections.shuffle(depotCustomers);

            int triesLeft = 100;
            for (Customer customer : depotCustomers) { // Assign customer to random vehicle
                boolean customerAdded = false;
                while (!customerAdded && triesLeft > 0) {
                    int randomIndex = Utils.randomIndex(depotVehicles.size()); // Random vehicle index
                    Vehicle randomVehicle = depotVehicles.get(randomIndex);

                    customerAdded = randomVehicle.smartAddCustomerToRoute(customer, force);

                    if (!customerAdded) {
                        triesLeft--;
                    }
                }

                if (triesLeft == 0) { // Giving up generating this initial isValid
                    return false;
                }
            }
            this.vehicles.addAll(depotVehicles);
        }


        // Optimize routes with swapping
        int swappingTriesLeft = 1000;
        int n = 1;
        double fitness = Double.MAX_VALUE;
        boolean swapped = true;

        while (swappingTriesLeft > 0 && n != 5) {
            System.out.println(swappingTriesLeft);
            calculateFitness();
            if (!swapped) {
                n++;
            }
            swapped = initialSwapping(n);
            swappingTriesLeft--;
        }

        if (isValid) {
            System.out.println("Individual is valid");
        } else {
            System.out.println("Individual is not valid");
        }

        return true;
    }

    /**
     * Tries to find the optimal place for customer(s)
     * n: Number of customers to find new place for
     * n is added 1 each time there is no more optimizing left for n
     */
    private boolean initialSwapping(int n) {

        // Copy vehicles to avoid concurrent error
        List<Vehicle> vehiclesCopy = deepCopyVehicles();

        // Vehicles where customers are
        for (int vehicleIndex = 0; vehicleIndex < vehicles.size(); vehicleIndex++) {
            // Customers to add from
            for (int routeIndex = 0; routeIndex < vehicles.get(vehicleIndex).getRoute().size(); routeIndex++) {

                // Makes sublist of customers based on n
                if (routeIndex + n <= vehicles.get(vehicleIndex).getRoute().size() - 1) {
                    List<Customer> customersToAdd = new ArrayList<>(vehicles.get(vehicleIndex).getRoute().subList(routeIndex, routeIndex + n));
                    vehiclesCopy.get(vehicleIndex).removeRouteFromRoute(customersToAdd);

                    // Set minFitness to currentFitness
                    double minFitness = getFitness();
                    int minVehicleIndex = vehicleIndex;
                    int minRouteIndex = routeIndex;


                    // Vehicles to put customer in
                    for (int i = 0; i < vehiclesCopy.size(); i++) {
                        // If route is size 0
                        if (vehiclesCopy.get(i).getRoute().size() == 0) {
                            double fitness = calculateFitnessIfRouteAdded(vehiclesCopy, vehiclesCopy.get(i), 0, customersToAdd);

                            // Better fitness?
                            if (fitness < minFitness) {
                                minFitness = fitness;
                                minVehicleIndex = i;
                                minRouteIndex = 0;
                            }
                        }

                        // Route indices to put customer in
                        for (int j = 0; j < vehiclesCopy.get(i).getRoute().size(); j++) {
                            double fitness = calculateFitnessIfRouteAdded(vehiclesCopy, vehiclesCopy.get(i), j, customersToAdd);

                            // Better fitness?
                            if (fitness < minFitness) {
                                minFitness = fitness;
                                minVehicleIndex = i;
                                minRouteIndex = j;
                            }
                        }
                    }

                    // Check if a new place is found for customer(s)
//                vehiclesCopy.get(vehicleIndex).removeRouteFromRoute(customersToAdd);
                    vehiclesCopy.get(minVehicleIndex).addOtherRouteToRoute(minRouteIndex, customersToAdd);
                    vehicles = vehiclesCopy;

                    int copySize = 0;
                    int orgSize = 0;
                    for (int k = 0; k < vehicles.size(); k++) {
                        copySize += vehiclesCopy.get(k).getRoute().size();
                        orgSize += vehicles.get(k).getRoute().size();
                    }

                    if (copySize != orgSize) {
                        System.out.println(copySize);
                        System.out.println(orgSize);
                        throw new Error("tabbe");
                    }

                    if (vehicleIndex != minVehicleIndex || routeIndex != minRouteIndex) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean generateRandomIndividual() {
        for (Depot depot : depots) {
            List<Vehicle> depotVehicles = createDepotVehicles(depot);
            List<Customer> depotCustomers = depot.getCustomers(); // Current depot's customers
            Collections.shuffle(depotCustomers);

            for (Customer customer : depotCustomers) { // Assign customer to random vehicle
                boolean customerAdded = false;
                int randomIndex = Utils.randomIndex(depotVehicles.size()); // Random vehicle index
                Vehicle randomVehicle = depotVehicles.get(randomIndex);

                while (!customerAdded) {
                    customerAdded = randomVehicle.addCustomerToRoute(customer);
                }
            }

            this.vehicles.addAll(depotVehicles);
        }
        return true;
    }

    /**
     * Performs n random mutations on the vehicles based on the mutationRate
     * Mutation is only executed if random < crossOverRate
     */
    public List<Vehicle> swapMutation() {
        // Copy of vehicles
        List<Vehicle> newVehicles = deepCopyVehicles();

        int randomIndex = Utils.randomIndex(vehicles.size());
        Vehicle vehicle = vehicles.get(randomIndex);
        List<Customer> newRoute = vehicle.swapMutate();
        Vehicle newVehicle = new Vehicle(vehicle.getStartDepot(), newRoute);

        newVehicles.remove(vehicle);
        newVehicles.add(newVehicle);

        return newVehicles;
    }

    public List<Vehicle> crossMutation() {
        // Copy of vehicles
        List<Vehicle> newVehicles = deepCopyVehicles();

        // Pick two random vehicles
        int randomIndex1 = Utils.randomIndex(vehicles.size());
        int randomIndex2 = Utils.randomIndex(vehicles.size());

        Vehicle randomVehicle1 = vehicles.get(randomIndex1).clone();
        Vehicle randomVehicle2 = vehicles.get(randomIndex2).clone();

        // Remove the old vehicles
        newVehicles.remove(randomVehicle1);
        newVehicles.remove(randomVehicle2);

        // Mutate the two vehicles and add them to newVehicles
        List<Customer>[] mutatedRoutes = randomVehicle1.crossMutate(randomVehicle2.getRoute());
        newVehicles.add(new Vehicle(randomVehicle1.getStartDepot(), randomVehicle2.getEndDepot(), mutatedRoutes[0]));
        newVehicles.add(new Vehicle(randomVehicle2.getStartDepot(), randomVehicle1.getEndDepot(), mutatedRoutes[1]));
        return newVehicles;
    }

    private List<Vehicle> createDepotVehicles(Depot depot) {
        List<Vehicle> vehicles = new ArrayList<>();

        for (int i = 0; i < depot.getMaxVehicles(); i++) {
            Vehicle v = new Vehicle(depot);
            vehicles.add(v);
        }
        return vehicles;
    }

    public void calculateFitness() {
        double calculatedFitness = 0.0;
        isValid = true;

        for (Vehicle vehicle : vehicles) {
            double penalty = 0;

            double maxDuration = vehicle.getStartDepot().getMaxDuration();
            double duration = vehicle.calculateRouteDuration();
            if (maxDuration != 0 && duration > maxDuration) {
                penalty += ((duration - maxDuration) * durationPenaltyRate);
            }

            double maxLoad = vehicle.getStartDepot().getMaxLoad();
            double load = vehicle.getCurrentLoad();
            if (maxLoad != 0 && load > maxLoad) {
                penalty += ((load - maxLoad) * loadPenaltyRate);
            }

            if (penalty > 0) {
                isValid = false;
            }

            calculatedFitness += (duration + penalty);
        }

        this.fitness = calculatedFitness;
    }

    public double getFitness() {
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

    /**
     * @param otherRoute
     * @return
     */

    public List<Vehicle> singlePointCrossOver(List<Customer> otherRoute) {
        if (vehicles == null) {
            throw new NullPointerException("No vehicles in solution");
        } else if (otherRoute.size() == 0) {
            return vehicles;
        }

        List<Vehicle> newVehicles = deepCopyVehicles();

        // Remove route from otherRoute
        removeRouteFromVehicles(newVehicles, otherRoute);

        // Rull gjennom alle ruter og regn ut diff i fitness på alle mulige steder
        double minFitnessIfAdded = Double.MAX_VALUE;
        Vehicle minVehicle = null;
        int minIndex = -1;

        for (Vehicle vehicle : newVehicles) {
            double fitnessIfAdded;

            if (vehicle.getRoute().size() == 0) {
                fitnessIfAdded = calculateFitnessIfRouteAdded(newVehicles, vehicle, 0, otherRoute);

                minFitnessIfAdded = fitnessIfAdded;
                minVehicle = vehicle;
                minIndex = 0;
            } else {
                for (int routeIndex = 0; routeIndex < vehicle.getRoute().size(); routeIndex++) {
                    fitnessIfAdded = calculateFitnessIfRouteAdded(newVehicles, vehicle, routeIndex, otherRoute);

                    if (fitnessIfAdded < minFitnessIfAdded) {
                        minFitnessIfAdded = fitnessIfAdded;
                        minVehicle = vehicle;
                        minIndex = routeIndex;
                    }
                }
            }

            setBestEndDepot(vehicle);
        }

        if (minVehicle == null) {
            throw new Error("MinVehicle is null");
        } else {
            minVehicle.addOtherRouteToRoute(minIndex, otherRoute);
        }

        return newVehicles;
    }

    private List<Vehicle> deepCopyVehicles() {
        // Creating a deep copy of vehicles
        List<Vehicle> newVehicles = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            newVehicles.add(vehicle.clone());
        }
        return newVehicles;
    }

    private void removeRouteFromVehicles(List<Vehicle> newVehicles, List<Customer> routeToRemove) {
        for (Vehicle vehicle : newVehicles) {
            List<Customer> routeCopy = new ArrayList<>(vehicle.getRoute()); // Copy to avoid error when removing while looping
            for (Customer customer : routeCopy) {
                if (routeToRemove.contains(customer)) {
                    vehicle.removeCustomerFromRoute(customer);
                }
            }
        }
    }

    private void setBestEndDepot(Vehicle vehicle) {
        if (vehicle.getRoute().size() > 0) {
            double currentMinDistance = Double.MAX_VALUE;
            Depot currentBestEndDepot = null;
            for (Depot depot : depots) {
                double distance = depot.distance(vehicle.getRoute().get(vehicle.getRoute().size() - 1));
                if (distance < currentMinDistance) {
                    currentMinDistance = distance;
                    currentBestEndDepot = depot;
                }
            }
            vehicle.setEndDepot(currentBestEndDepot);
        }
    }

    private double calculateFitnessIfRouteAdded(List<Vehicle> vehicles, Vehicle vehicle, int addIndex, List<Customer> routeToAdd) {
        List<Vehicle> originalVehiclesCopy = this.vehicles;
        this.vehicles = vehicles;
        double originalFitness = fitness;
        vehicle.addOtherRouteToRoute(addIndex, routeToAdd);
        calculateFitness();
        double newFitness = fitness;
        vehicle.removeRouteFromRoute(routeToAdd);
        this.fitness = originalFitness;
        this.vehicles = originalVehiclesCopy;
        return newFitness;
    }

    private double calculateFitnessIfCustomerAdded(List<Vehicle> vehicles, Vehicle vehicle, int addIndex, Customer customerToAdd) {
        List<Vehicle> originalVehiclesCopy = this.vehicles;
        this.vehicles = vehicles;
        double originalFitness = fitness;
        vehicle.addCustomerToRoute(addIndex, customerToAdd);
        calculateFitness();
        double newFitness = fitness;
        vehicle.removeCustomerFromRoute(customerToAdd);
        this.fitness = originalFitness;
        this.vehicles = originalVehiclesCopy;
        return newFitness;
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
            writer.write(vehicle.getStartDepot().getId() + "\t" // s: number of the start depot
                    + vehicle.getStartDepot().getMaxVehicles() + "\t" // k: number of the vehicle (for above depot)
                    + Utils.round(vehicle.calculateRouteDuration(), 2) + "   " // d: duration of the route for a particular vehicle from a particular depot
                    + vehicle.getCurrentLoad() + "\t" // q: carried load of the vehicle
                    + vehicle.getEndDepot().getId() + "\t" // e: number of the end depot
                    + vehicle.getRoute().toString() // list: ordered sequence of customers (served by a particular vehicle)
                    .replace(",", "")  // remove the commas
                    .replace("[", "")  // remove the right bracket
                    .replace("]", "")  // remove the left bracket
            );
        }
        writer.close();
    }

    public void appendToCSV(int populationSize, double crossOverRate, double mutationRate, int k, List<Double> generations) throws IOException {
        File file = new File("results.csv");

        if (!file.exists()) {
            file.createNewFile();
            createCsv();
        }

        FileWriter csvWriter = new FileWriter("results.csv", true);
        csvWriter.append("\n")
                .append(Controller.fileName)
                .append(", ")
                .append(String.valueOf(populationSize))
                .append(", ")
                .append(String.valueOf(crossOverRate))
                .append(", ")
                .append(String.valueOf(mutationRate))
                .append(", ")
                .append(String.valueOf(k));

        for (int i = 0; i < 10000; i++) {
            if (i > generations.size() - 1) {
                csvWriter.append(", ").append(String.valueOf(generations.get(generations.size() - 1)));
            } else {
                csvWriter.append(", ").append(String.valueOf(generations.get(i)));
            }
        }
        csvWriter.close();
    }

    private void createCsv() throws IOException {

        FileWriter csvWriter = new FileWriter("results.csv");
        csvWriter.append("Map, Population size, Crossover rate, Mutation rate, k");

        for (int i = 1; i <= 10000; i++) {
            csvWriter.append(", ").append(String.valueOf(i));
        }
        csvWriter.close();
    }

}
