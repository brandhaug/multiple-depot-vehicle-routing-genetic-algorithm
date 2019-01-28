package Map;

import MapObjects.Customer;
import MapObjects.Depot;
import MapObjects.Vehicle;
import javafx.scene.canvas.GraphicsContext;

import java.io.IOException;
import java.util.List;

public class Map {
    // Lists
    private List<Depot> depots;
    private List<Customer> customers;
    private List<Vehicle> vehicles;

    // Map values
    public static int maximumX = -Integer.MAX_VALUE;
    public static int maximumY = -Integer.MAX_VALUE;
    public static int minimumX = Integer.MAX_VALUE;
    public static int minimumY = Integer.MAX_VALUE;
    public static double scaleX;
    public static double scaleY;

    public Map(String fileName) throws IOException {
        MapParser mapParser = new MapParser(fileName);
        this.depots = mapParser.getDepots();
        this.customers = mapParser.getCustomers();
        this.vehicles = mapParser.getVehicles();
    }

    public void tick() {
    }

    public void render(GraphicsContext gc) {
        renderDepots(gc);
        renderCustomers(gc);
    }

    private void renderDepots(GraphicsContext gc) {
        for (Depot depot : depots) {
            depot.render(gc);
        }
    }

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
}
