package MapObjects;

import Main.Controller;
import Main.Map;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class MapObject {

    private int x;
    private int y;

    /**
     * Sets the x and y position of the game object
     *
     * @param x the x position
     * @param y the y position
     */
    MapObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void tick();

    public abstract void render(GraphicsContext gc);

    public int getPixelX() {
        return x * Map.scaleX;
    }

    public int getPixelY() {
        return y * Map.scaleY;
    }

    public void setCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
