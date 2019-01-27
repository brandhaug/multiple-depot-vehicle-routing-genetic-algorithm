package MapObjects;

import Utils.Utils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Customer extends MapObject {
    private int id; // i: customer number
    private int serviceDuration; // d: necessary service duration required for this customer
    private int loadDemand; // q: demand for this customer
    private Depot depot;

    public Customer(int id, int x, int y, int serviceDuration, int loadDemand) {
        super(x, y);
        this.id = id;
        this.serviceDuration = serviceDuration;
        this.loadDemand = loadDemand;
    }

    /**
     * Calculates euclidean distance between two Customers
     * TODO: We may need this function for both Customers and Depots
     *
     * @param otherCustomer
     * @return
     */
    double distance(Customer otherCustomer) {
        return Utils.euclideanDistance(getX(), otherCustomer.getX(), getY(), otherCustomer.getY());
    }

    @Override
    public void tick() {

    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setStroke(Color.GRAY);
        gc.strokeOval(getPixelX() - 2, getPixelY() - 2, 5, 5);
    }

    @Override
    public String toString() {
        return Integer.toString(id);
    }

    public void setDepot(Depot depot) {
    }
}
