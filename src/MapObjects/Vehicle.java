package MapObjects;

import javafx.scene.canvas.GraphicsContext;

public class Vehicle extends MapObject {
    private Depot depot;
    private boolean checkedIn;

    public Vehicle(Depot depot) {
        super(depot.getX(), depot.getY());
        this.depot = depot;
    }

    @Override
    public void tick() {

    }

    @Override
    public void render(GraphicsContext gc) {
        //TODO: Render line

    }
}
