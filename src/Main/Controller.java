package Main;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;

import java.io.IOException;

public class Controller {

    public final static int CANVAS_WIDTH = 500;
    public final static int CANVAS_HEIGHT = 500;
    public final static int CANVAS_MARGIN = 32;

    @FXML
    private Canvas canvas;

    @FXML
    private Button startButton;

    private Map map;

    // Canvas
    private GraphicsContext gc;

    // States
    private boolean initialized = false;
    private boolean paused = true;

    private void render() {
        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        map.render(gc);
    }

    /**
     * Application loop
     *
     * @param startNanoTime   the nano time of when the game was initialized.
     * @param currentNanoTime the nano time of the current game loop.
     */
    private void tick(long startNanoTime, long currentNanoTime) {
        double time = (currentNanoTime - startNanoTime) / 1000000000.0;
        map.tick();
        render();
    }


    @FXML
    public void start(ActionEvent actionEvent) {
        paused = false;
    }

    @FXML
    public void initialize() {
        try {
            map = new Map("p01");
        } catch (IOException e) {
            e.printStackTrace();
        }

        gc = canvas.getGraphicsContext2D();
        final long startNanoTime = System.nanoTime();

        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                if (!paused) {
                    tick(startNanoTime, currentNanoTime);
                }
            }
        }.start();
    }
}
