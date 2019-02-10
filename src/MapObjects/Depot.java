package MapObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.List;

/**
 * Customer's are assigned to closest Depot
 * Each depot is given m Vehicles
 */
public class Depot extends MapObject {
    private String id;
    private double maxDuration; // D: maximum duration of a route
    private int maxLoad; // Q: allowed maximum load of a vehicle
    private int maxVehicles; // m: maximum number of vehicles available in each depot

    private static int colorIndex;

    private Color[] colors = {Color.RED, Color.ORANGE, Color.GOLD, Color.GREENYELLOW, Color.GREEN, Color.AQUA, Color.BLUE, Color.INDIGO, Color.VIOLET}; // Possible depot colors
    private Color color; // Depot color on canvas

    private List<Customer> customers;


    public Depot(int maxDuration, int maxLoad, int maxVehicles) {
        super(0, 0);
        this.id = id;
        this.customers = new ArrayList<>();
        this.maxDuration = maxDuration;
        this.maxLoad = maxLoad;
        this.maxVehicles = maxVehicles;
        this.color = colors[colorIndex]; // Sets color based on static counter

        // Updates static counter
        if (colorIndex == colors.length - 1) {
            colorIndex = 0;
        } else {
            colorIndex++;
        }
    }

    /**
     * Draws Depot on canvas
     * Represented as a colored dot
     *
     * @param gc
     */
    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(getPixelX() - 5, getPixelY() - 5, 10, 10);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getMaxDuration() {
        return maxDuration;
    }

    public int getMaxLoad() {
        return maxLoad;
    }

    public int getMaxVehicles() {
        return maxVehicles;
    }

    public Paint getColor() {
        return color;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }
}
