package MapObjects;

import Main.Controller;
import Map.Map;
import Utils.Utils;
import javafx.scene.canvas.GraphicsContext;

public abstract class MapObject {

    private int x;
    private int y;

    /**
     * Sets the x and y position of the game object
     * @param x the x position
     * @param y the y position
     */
    MapObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Draws object on canvas
     * @param gc
     */
    public abstract void render(GraphicsContext gc);

    /**
     * Calculates euclidean distance between two Customers
     * @param otherMapObject
     * @return
     */
    public double distance(MapObject otherMapObject) {
        return Utils.euclideanDistance(getX(), otherMapObject.getX(), getY(), otherMapObject.getY());
    }

    /**
     * Translate x to pixel on canvas
     * @return
     */
    public int getPixelX() {
        return (int) ((x - Map.minimumX + (Controller.CANVAS_MARGIN / 2)) * Map.scaleX);
    }

    /**
     * Translate y to pixel on canvas
     * @return
     */
    public int getPixelY() {
        return (int) ((y - Map.minimumY + (Controller.CANVAS_MARGIN / 2)) * Map.scaleY);
    }

    public void setCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

}
