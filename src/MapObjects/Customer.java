package MapObjects;

import Utils.Utils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Should be visited once by a Vehicle
 * Contained in a Vehicle's route
 */
public class Customer extends MapObject {
    private int id; // i: customer number
    private int serviceDuration; // TODO: d: necessary service duration required for this customer
    private int loadDemand; // TODO: q: demand for this customer

    public Customer(int id, int x, int y, int serviceDuration, int loadDemand) {
        super(x, y);
        this.id = id;
        this.serviceDuration = serviceDuration;
        this.loadDemand = loadDemand;
    }

    /**
     * Draws customer on canvas
     * Represented as a gray circle
     * @param gc
     */
    @Override
    public void render(GraphicsContext gc) {
        gc.setStroke(Color.GRAY);
        gc.strokeOval(getPixelX() - 2, getPixelY() - 2, 5, 5);
    }

    /**
     * Calculates euclidean distance between two Customers
     * TODO: We may need this function for both Customers and Depots. If so, move it to MapObject
     * @param otherCustomer
     * @return
     */
    public double distance(Customer otherCustomer) {
        return Utils.euclideanDistance(getX(), otherCustomer.getX(), getY(), otherCustomer.getY());
    }

    /**
     * Prints CustomerId
     * Used to print Customers in a Vehicle's route
     */
    @Override
    public String toString() {
        return Integer.toString(id);
    }
}
