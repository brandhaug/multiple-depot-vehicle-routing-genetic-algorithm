package Map;

import MapObjects.Customer;
import MapObjects.Depot;
import MapObjects.Vehicle;
import javafx.scene.canvas.GraphicsContext;

import java.io.IOException;
import java.util.List;

/**
 * Map is a visual representation of the map file
 * Draws Depots and Customers on Canvas
 */
public class Map {
    // Lists
    private List<Depot> depots; // All depots in map
    private List<Customer> customers; // All customers in map
    private List<Vehicle> vehicles; // All vehicles in map
    private double benchmarkFitness;

    // Map extreme values: used to calculate scales
    public static int maximumX = -Integer.MAX_VALUE; // Largest x-value in map
    public static int maximumY = -Integer.MAX_VALUE; // Largest y-value in map
    public static int minimumX = Integer.MAX_VALUE; // Smallest x-value in map
    public static int minimumY = Integer.MAX_VALUE; // Smallest y-value in map

    // Scales
    public static double scaleX; // Scales map to correct width in canvas
    public static double scaleY; // Scales map to correct height in canvas

    /**
     * Creates mapParser which creates Objects (Depots, Customers, Vehicles) from file
     * Retrieves Objects back to class
     * @param fileName
     * @throws IOException
     */
    public Map(String fileName) throws IOException {
        MapParser mapParser = new MapParser(fileName);
        this.depots = mapParser.getDepots();
        this.customers = mapParser.getCustomers();
        this.vehicles = mapParser.getVehicles();
        this.benchmarkFitness = mapParser.getBenchmark();
    }

    /**
     * Functionality loop
     */
    public void tick() {
    }

    /**
     * Draws depots and customers in canvas
     * @param gc
     */
    public void render(GraphicsContext gc) {
        renderDepots(gc);
        renderCustomers(gc);
    }

    /**
     * Draws depots in canvas
     * @param gc
     */
    private void renderDepots(GraphicsContext gc) {
        for (Depot depot : depots) {
            depot.render(gc);
        }
    }

    /**
     * Draws customers in canvas
     * @param gc
     */
    private void renderCustomers(GraphicsContext gc) {
        for (Customer customer : customers) {
            customer.render(gc);
        }
    }

    public int getDepotsSize() {
        return depots.size();
    }

    public int getVehiclesSize() {
        return vehicles.size();
    }

    public int getCustomersSize() {
        return customers.size();
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
