package Main;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class Controller {

    private String fileName = "p01";

    @FXML
    private Canvas canvas;

    @FXML
    private Button startButton;

    @FXML
    private Label mapLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label depotsLabel;

    @FXML
    private Label vehiclesLabel;

    @FXML
    private Label customersLabel;

    @FXML
    private Label generationLabel;

    @FXML
    private Label fitnessLabel;

    private Map map;

    // Canvas
    public final static int CANVAS_WIDTH = 500;
    public final static int CANVAS_HEIGHT = 500;
    public final static int CANVAS_MARGIN = 10;
    private GraphicsContext gc;

    //Genetic Algorithm
    private int generation = 0;
    private int fitness = 0;

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
        generationLabel.setText("Generation: " + ++generation);
        timeLabel.setText("Time: " + (int) time);
        map.tick();
    }


    @FXML
    public void togglePaused(ActionEvent actionEvent) {
        paused = !paused;

        if (paused) {
            startButton.setText("Start");
        } else {
            startButton.setText("Pause");
        }

    }

    @FXML
    public void initialize() {
        try {
            map = new Map(fileName);
            mapLabel.setText("Map: " + fileName);
            depotsLabel.setText("Depots: " + map.getDepotsSize());
            vehiclesLabel.setText("Vehicles: " + map.getVehiclesSize());
            customersLabel.setText("Customers: " + map.getCustomersSize());
        } catch (IOException e) {
            e.printStackTrace();
        }

        gc = canvas.getGraphicsContext2D();
        render();
        final long startNanoTime = System.nanoTime();

        //TODO: Mulig det er bedre å kjøre tick i en egen loop, også ha render i en "Timer" med delay
        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                if (!paused) {
                    tick(startNanoTime, currentNanoTime);
                    render();
                }
            }
        }.start();
    }


}
