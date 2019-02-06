package MapObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Should be visited once by a Vehicle
 * Contained in a Vehicle's route
 */
public class Customer extends MapObject {
    private String id; // i: customer number
    private int timeDemand;
    private int loadDemand;

    public Customer(String id, int x, int y, int timeDemand, int loadDemand) {
        super(x, y);
        this.id = id;
        this.timeDemand = timeDemand;
        this.loadDemand = loadDemand;
    }

    /**
     * Draws customer on canvas
     * Represented as a gray circle
     */
    @Override
    public void render(GraphicsContext gc) {
        gc.setStroke(Color.GRAY);
        gc.strokeOval(getPixelX() - 2, getPixelY() - 2, 5, 5);
    }

    public int getLoadDemand() {
        return loadDemand;
    }

    public int getTimeDemand() {
        return timeDemand;
    }

    @Override
    public String toString() {
        return id;
    }
}
