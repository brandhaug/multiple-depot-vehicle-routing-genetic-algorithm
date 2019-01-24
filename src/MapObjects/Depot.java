package MapObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Depot extends MapObject {
    private int maxDistance; // D: maximum duration of a route
    private int maxLoad; // Q: allowed maximum load of a vehicle
    private int maxCars; // m: maximum number of vehicles available in each depot
    private static int depotIndex;
    private Color[] colors = {Color.RED, Color.ORANGE, Color.GOLD, Color.GREEN, Color.BLUE, Color.INDIGO, Color.VIOLET};
    private Color color;


    public Depot(int maxDistance, int maxLoad, int maxCars) {
        super(0, 0);
        this.maxDistance = maxDistance;
        this.maxLoad = maxLoad;
        this.maxCars = maxCars;
        this.color = colors[depotIndex];

        if (depotIndex == colors.length - 1) {
            depotIndex = 0;
        } else {
            depotIndex++;
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(getPixelX(), getPixelY(), 10, 10);
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public int getMaxLoad() {
        return maxLoad;
    }

    public void setMaxLoad(int maxLoad) {
        this.maxLoad = maxLoad;
    }

    public int getMaxCars() {
        return maxCars;
    }

    public void setMaxCars(int maxCars) {
        this.maxCars = maxCars;
    }
}
