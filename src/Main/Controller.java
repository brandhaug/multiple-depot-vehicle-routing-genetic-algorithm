package Main;

import GeneticAlgorithm.GeneticAlgorithm;
import Map.Map;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;

public class Controller {

    @FXML private Canvas canvas;
    @FXML private Button startButton;
    @FXML private Label mapLabel;
    @FXML private Label timeLabel;
    @FXML private Label depotsLabel;
    @FXML private Label vehiclesLabel;
    @FXML private Label customersLabel;
    @FXML private Label generationLabel;
    @FXML private Label fitnessLabel;

    // Map
    private Map map;
    private String fileName = "p01";

    private GeneticAlgorithm ga;

    // Canvas
    public final static int CANVAS_WIDTH = 500;
    public final static int CANVAS_HEIGHT = 500;
    public final static int CANVAS_MARGIN = 10;
    private GraphicsContext gc;

    // States
    private boolean paused = true;

    // Settings
    public static boolean verbose = true;

    @FXML
    private void initialize() {
        try {
            map = new Map(fileName);
            ga = new GeneticAlgorithm(map.getDepots(), map.getVehicles());
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

        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                if (!paused) {
                    tick(startNanoTime, currentNanoTime);
                    render();
                }
            }
        }.start();
    }

    /**
     * Application loop
     *
     * @param startNanoTime   the nano time of when the game was initialized.
     * @param currentNanoTime the nano time of the current game loop.
     */
    private void tick(long startNanoTime, long currentNanoTime) {
        if (verbose) {
            System.out.println("Generation: " + ga.getGeneration());
        }

        map.tick();
        ga.tick();
        updateGUI(startNanoTime, currentNanoTime);
    }

    private void render() {
        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        map.render(gc);
    }

    private void updateGUI(long startNanoTime, long currentNanoTime) {
        double time = (currentNanoTime - startNanoTime) / 1000000000.0;
        generationLabel.setText("Generation: " + ga.getGeneration());
        timeLabel.setText("Time: " + (int) time);
        fitnessLabel.setText("Fitness: " + ga.getAlphaFitness());
    }


    @FXML
    private void togglePaused() {
        paused = !paused;

        if (paused) {
            startButton.setText("Start");
        } else {
            startButton.setText("Pause");
        }

    }
}
