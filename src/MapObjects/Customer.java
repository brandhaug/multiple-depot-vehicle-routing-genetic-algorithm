package MapObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Customer extends MapObject {
    private int id; // i: customer number
    private int serviceDuration; // d: necessary service duration required for this customer
    private int loadDemand; // q: demand for this customer

    public Customer(int id, int x, int y, int serviceDuration, int loadDemand) {
        super(x, y);
        this.id = id;
        this.serviceDuration = serviceDuration;
        this.loadDemand = loadDemand;
    }

    @Override
    public void tick() {

    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.GRAY);
        gc.fillOval(getPixelX(), getPixelY(), 5, 5);
    }
}
