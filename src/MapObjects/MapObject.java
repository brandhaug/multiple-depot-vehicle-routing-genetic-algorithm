package MapObjects;

import Main.Controller;
import Map.Map;
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
     * Translate x to pixel on canvas
     * @return
     */
    public int getPixelX() {
        int pixelX = (int) ((x - Map.minimumX + (Controller.CANVAS_MARGIN / 2)) * Map.scaleX);
        if (pixelX >= Controller.CANVAS_WIDTH) {
            System.out.println("WARNING: PixelX=" + pixelX + " from x=" + x + " is >= CANVAS_WIDTH=" + Controller.CANVAS_WIDTH);
        } else if (pixelX <= 0) {
            System.out.println("WARNING: PixelX=" + pixelX + " from x=" + x + " is <= 0");
        }
        return pixelX;
    }

    /**
     * Translate y to pixel on canvas
     * @return
     */
    public int getPixelY() {
        int pixelY = (int) ((y - Map.minimumY + (Controller.CANVAS_MARGIN / 2)) * Map.scaleY);
        if (pixelY >= Controller.CANVAS_HEIGHT) {
            System.out.println("WARNING: PixelY=" + pixelY + " from y=" + y + " is >= CANVAS_HEIGHT=" + Controller.CANVAS_HEIGHT);
        } else if (pixelY <= 0) {
            System.out.println("WARNING: PixelY=" + pixelY + " from y=" + y + " is <= 0");
        }
        return pixelY;
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
