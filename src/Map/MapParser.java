package Map;

import Main.Controller;
import Utils.Utils;
import MapObjects.Customer;
import MapObjects.Depot;
import MapObjects.Vehicle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Parses map file
 */
public class MapParser {
    private List<Depot> depots = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private List<Vehicle> vehicles = new ArrayList<>();
    private double benchmarkFitness;

    /**
     * Parser map file
     * Assigns customers to nearest depot
     * @param fileName
     * @throws IOException
     */
    public MapParser(String fileName) throws IOException {
        parseMapFile(fileName);
        assignCustomersToNearestDepot();
        parseResult(fileName);
    }


    /**
     * Parses map file
     * @param fileName
     * @throws IOException
     */
    private void parseMapFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("resources/maps/" + fileName).getFile());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        int index = 0;
        int depotIndex = 0;

        int maxVehicles = 0; // m: maximum number of vehicles available in each depot
        int totalCustomers = 0; // n: total number of customers
        int depotsCount = 0; // t: number of depots

        if (Controller.verbose) {
            System.out.println("========= Parsing map file =========");
        }
        while ((line = br.readLine()) != null) {
            String[] stringLineArr = line.trim().split("\\s+");
            int[] lineArr = Arrays.stream(stringLineArr).mapToInt(Integer::parseInt).toArray();

            if (index == 0) { // Map info: m n t
                if (Controller.verbose) {
                    System.out.println("Map info: " + line);
                }
                maxVehicles = lineArr[0];
                totalCustomers = lineArr[1];
                depotsCount = lineArr[2];
            } else if (index <= depotsCount) { // Depot info: The next t lines contain, the following information: D Q
                if (Controller.verbose) {
                    System.out.println("Depot info: " + line);
                }
                Depot depot = new Depot(lineArr[0], lineArr[1], maxVehicles);
                depots.add(depot);
            } else if (index <= depotsCount + totalCustomers) { // Customer: id, x, y, d, q
                if (Controller.verbose) {
                    System.out.println("Customer info: " + line);
                }
                Customer customer = new Customer(Integer.toString(lineArr[0]), lineArr[1], lineArr[2], lineArr[3], lineArr[4]);
                customers.add(customer);
                setExtremeValues(lineArr[1], lineArr[2]);

            } else if (depotIndex <= depotsCount) { // Depot coordinates: id, x, y
                if (Controller.verbose) {
                    System.out.println("Depot location: " + line);
                }
                Depot depot = depots.get(depotIndex);
                depot.setId(Integer.toString(lineArr[0]));
                depot.setCoordinates(lineArr[1], lineArr[2]);
                setExtremeValues(lineArr[1], lineArr[2]);

                for (int i = 0; i < maxVehicles; i++) {
                    Vehicle vehicle = new Vehicle(depot);
                    vehicles.add(vehicle);
                }

                depotIndex++;
            } else {
                System.out.println("Oh no, I shouldn't be here!");
            }
            index++;
        }

        Map.scaleX = calculcateScaling(Map.maximumX, Map.minimumX, Controller.CANVAS_WIDTH);
        Map.scaleY = calculcateScaling(Map.maximumY, Map.minimumY, Controller.CANVAS_HEIGHT);

        if (Controller.verbose) {
            System.out.println("minimumX: " + Map.minimumX);
            System.out.println("maximumX: " + Map.maximumX);
            System.out.println("scaleX: " + Map.scaleX);
            System.out.println("minimumY: " + Map.minimumY);
            System.out.println("maximumY: " + Map.maximumY);
            System.out.println("scaleY: " + Map.scaleY);
            System.out.println("========= END Parsing map file =========");
        }
    }

    private void parseResult(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("resources/solutions/" + fileName + ".res").getFile());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        if (Controller.verbose) {
            System.out.println("========= Parsing result file =========");
        }

        line = br.readLine();
        if (line  != null) {
            benchmarkFitness = Double.parseDouble(line);
        }

        if (Controller.verbose) {
            System.out.println("========= END Parsing result file =========");
        }
    }

    /**
     * Sets extreme values, if more extreme than the current extreme value.
     * @param x
     * @param y
     */
    private void setExtremeValues(int x, int y) {
        Map.maximumX = Math.max(x, Map.maximumX);
        Map.maximumY = Math.max(y, Map.maximumY);
        Map.minimumX = Math.min(x, Map.minimumX);
        Map.minimumY = Math.min(y, Map.minimumY);
    }

    /**
     * Assigns customers to nearest depot
     * Based on euclidean distance
     */
    private void assignCustomersToNearestDepot() {
        Depot nearestDepot = null;
        for (Customer customer : customers) {
            double minimumDistance = Double.MAX_VALUE;
            for (Depot depot : depots) {
                double distance = Utils.euclideanDistance(customer.getX(), depot.getX(), customer.getY(), depot.getY());

                if (distance < minimumDistance) {
                    minimumDistance = distance;
                    nearestDepot = depot;
                }
            }

            if (nearestDepot == null) {
                throw new NullPointerException("Nearest Depot is not set");
            }

            nearestDepot.addCustomer(customer);
        }
    }

    /**
     * Calculates scaling used to scale map to canvas
     * @param maximum
     * @param minimum
     * @param canvasSize
     * @return
     */
    private double calculcateScaling(int maximum, int minimum, int canvasSize) {
        int variance = maximum - minimum;
//        double scaleMargin = (double) Controller.CANVAS_MARGIN / variance;
//        return (double) (canvasSize / variance) - scaleMargin;
        return (double) canvasSize / (maximum + variance);
    }

    public List<Depot> getDepots() {
        return depots;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public double getBenchmark()
    {
        return benchmarkFitness;
    }
}
