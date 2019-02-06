package Main;

import GeneticAlgorithm.GeneticAlgorithm;
import Map.Map;
import Utils.Utils;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controls GUI (View.fxml)
 * Controls Map and GeneticAlgorithms
 * Creates Animation with AnimationTimer
 * Each loop in Animation is the main game loop.
 * The game loop consists of two main functions: tick() and render()
 * tick() fires functionality
 * render() draws on canvas
 */
public class Controller {

    // GUI
    @FXML private Button startButton; // Toggles between "Start" and "Pause", depending on state
    @FXML private Label mapLabel; // Shows current Map
    @FXML private Label timeLabel; // Shows current time
    @FXML private Label depotsLabel; // Shows number of depots in Map
    @FXML private Label vehiclesLabel; // Shows number of vehicles in Map
    @FXML private Label customersLabel; // Shows number of customers in Map
    @FXML private Label generationLabel; // Shows generation in GeneticAlgorithm
    @FXML private Label fitnessLabel;  // Shows alphaFitness (best Solution) of Population in GeneticAlgorithm
    @FXML private Label benchmarkLabel; // Shows benchmark fitness for current map
    @FXML private ComboBox mapSelector; // Shows benchmark fitness for current map

    // Map
    @FXML private Canvas canvas;
    private Map map;
    private String fileName = "p01"; // Current map

    private GeneticAlgorithm ga; // GeneticAlgorithm: Contains a Population, which contains Solutions

    // Canvas
    public final static int CANVAS_WIDTH = 500; // Canvas width set in View.fxml
    public final static int CANVAS_HEIGHT = 500; // Canvas width set in View.fxml
    public final static int CANVAS_MARGIN = 10; // The margin avoids that extreme points are drawn outside canvas
    private GraphicsContext gc; // Used to draw on canvas

    // States
    private boolean paused = true; // Used to start/pause game loop
    private boolean initialized = false; // Used to start/pause game loop

    // Settings
    public static boolean verbose = false; // Used to enable logging with System.out.println()

    /**
     * Initializes GUI
     * Initializes Map (parses file)
     * Initializes GeneticAlgorithm
     */
    @FXML
    private void initialize() {
        try {
            map = new Map(fileName); // Parse file
            ga = new GeneticAlgorithm(map.getDepots());
            initializeGUI();
        } catch (IOException e) {
            e.printStackTrace();
        }

        gc = canvas.getGraphicsContext2D(); // Used to draw in canvas
        render(); // Render Map (Depots and Customers) and alphaSolution (best Solution in population) on canvas
        final long startNanoTime = System.nanoTime(); // Time when system starts
        initialized = true;

        new AnimationTimer() { // Game loop
            public void handle(long currentNanoTime) {
                if (!paused) {
                    tick(startNanoTime, currentNanoTime);
                    render();
                }

                double alphaFitness = ga.getAlphaFitness();
                if (alphaFitness != -1 && alphaFitness <= map.getBenchmark()) {
                    paused = true;
                    fitnessLabel.setText("Fitness: " + Utils.round(ga.getAlphaFitness(), 2));
                    startButton.setText("Start");
                }
            }
        }.start();
    }

    /**
     * Functionality loop
     * Creates next generation of Population in GeneticAlgorithm
     *
     * @param startNanoTime   the nano time of when the game was initialized.
     * @param currentNanoTime the nano time of the current game loop.
     */
    private void tick(long startNanoTime, long currentNanoTime) {
        if (verbose) {
            System.out.println("Generation: " + ga.getGeneration());
        }

        ga.tick(); // Takes Population in GeneticAlgorithm to next generation
        updateGUI(startNanoTime, currentNanoTime); // Updates labels
    }

    /**
     * Canvas draw loop
     */
    private void render() {
        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT); // Clear canvas
        map.render(gc); // Renders depots and customers
        ga.render(gc); // Renders alphaSolution of Population in GeneticAlgorithm
    }

    private void initializeGUI() {
        mapLabel.setText("Map: " + fileName); // Current map
        depotsLabel.setText("Depots: " + map.getDepotsSize()); // Number of depots
        vehiclesLabel.setText("Vehicles: " + map.getVehiclesSize()); // Number of vehicles
        customersLabel.setText("Customers: " + map.getCustomersSize()); // Number of customers
        benchmarkLabel.setText("Benchmark: " + map.getBenchmark());

        if (!initialized) {
            initializeMapSelector();
        }
    }

    private void initializeMapSelector() {
        ClassLoader classLoader = getClass().getClassLoader();
        File folder = new File(classLoader.getResource("resources/maps").getFile());
        File[] mapFiles = folder.listFiles();
        Arrays.sort(mapFiles);

        if (mapFiles.length == 0) {
            throw new IllegalStateException("Map folder is empty");
        }

        List<String> mapNames = new ArrayList<>();

        for (File file : mapFiles) {
            if (file.isFile()) {
                mapNames.add(file.getName());
            }
        }

        mapSelector.setItems(FXCollections.observableArrayList(mapNames));
        mapSelector.getSelectionModel().selectFirst();
    }

    /**
     * Updates labels in GUI
     *
     * @param startNanoTime   the nano time of when the game was initialized.
     * @param currentNanoTime the nano time of the current game loop.
     */
    private void updateGUI(long startNanoTime, long currentNanoTime) {
        double time = (currentNanoTime - startNanoTime) / 1000000000.0;
        generationLabel.setText("Generation: " + ga.getGeneration());
        timeLabel.setText("Time: " + (int) time);
        fitnessLabel.setText("Fitness: " + Utils.round(ga.getAlphaFitness(), 2));
    }

    /**
     * Toggles paused state on button click
     */
    @FXML
    private void togglePaused() {
        paused = !paused;

        if (paused) {
            startButton.setText("Start");
        } else {
            startButton.setText("Pause");
        }
    }

    @FXML
    private void selectMap() {
        fileName = mapSelector.getValue().toString();
        initialize();
        paused = true;
    }
}
