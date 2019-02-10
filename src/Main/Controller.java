package Main;

import GeneticAlgorithm.GeneticAlgorithm;
import Map.Map;
import Utils.Utils;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    @FXML private AnchorPane anchorPane;
    @FXML private Button startButton; // Toggles between "Start" and "Pause", depending on state
    @FXML private Button resetButton;
    @FXML private Button saveButton;
    @FXML private Button appendButton;
    @FXML private Label timeLabel; // Shows current time
    @FXML private Label depotsLabel; // Shows number of depots in Map
    @FXML private Label vehiclesLabel; // Shows number of vehicles in Map
    @FXML private Label customersLabel; // Shows number of customers in Map
    @FXML private Label generationLabel; // Shows generation in GeneticAlgorithm
    @FXML private Label fitnessLabel; // Shows alphaFitness (best Individual) of Population in GeneticAlgorithm
    @FXML private Label durationLabel;  // Shows duration of alpha individual of Population in GeneticAlgorithm
    @FXML private Label benchmarkLabel; // Shows benchmark fitness for current map
    @FXML private ComboBox mapSelector; // Shows benchmark fitness for current map
    @FXML private LineChart lineChart; // Shows statistics
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    // Map
    @FXML private Canvas canvas;

    private Map map;
    public static String fileName = "p01"; // Current map

    private GeneticAlgorithm ga; // GeneticAlgorithm: Contains a Population, which contains Solutions

    // Line chart
    private XYChart.Series seriesAlphaSolution; // Fitness per generation of alpha solution
    private XYChart.Series seriesPopulation; // Average fitness per generation of population
    private HashMap<Integer, Double> alphaSolutionFitnessData;
    private HashMap<Integer, Double> populationFitnessData;
    private boolean shouldUpdate = false;

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
    private boolean viewGraph = false; // Used to enable/disable viewGraph

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

        if (viewGraph) {
            initializeLineChart();
            anchorPane.setPrefHeight(750);
            lineChart.setLayoutY(500);
            lineChart.setPrefHeight(250);
            lineChart.setPrefWidth(650);
        } else {
            anchorPane.setPrefHeight(CANVAS_HEIGHT);
            lineChart.setVisible(false);
        }


        gc = canvas.getGraphicsContext2D(); // Used to draw in canvas
        render();
        final long startNanoTime = System.nanoTime(); // Time when system starts
        initialized = true;

        new AnimationTimer() { // Game loop
            public void handle(long currentNanoTime) {
                if (!paused) {
                    tick(startNanoTime, currentNanoTime);

                    if (viewGraph) {
                        alphaSolutionFitnessData.put(ga.getGeneration(), ga.getAlphaFitness());
                        populationFitnessData.put(ga.getGeneration(), ga.getAverageFitness());
                        shouldUpdate = true;
                    }

                    render();
                }

                if (paused && ga.getAlphaSolution() != null && viewGraph) {
                    updateLinechart();
                }

                if (ga.getAlphaSolution() != null && ga.getAlphaDuration() <= map.getBenchmark() && ga.isAlphaValid()) { // Reached benchmark
                    paused = true;
                    durationLabel.setText("Duration: " + Utils.round(ga.getAlphaDuration(), 2));
                    fitnessLabel.setText("Fitness: " + Utils.round(ga.getAlphaFitness(), 2));
                    durationLabel.setStyle("-fx-font-weight: bold");
                    startButton.setVisible(false);
                    startButton.setText("Start");
                    saveButton.setVisible(true);
                    appendButton.setVisible(true);
                    mapSelector.setVisible(true);
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
        depotsLabel.setText("Depots: " + map.getDepotsSize()); // Number of depots
        vehiclesLabel.setText("Vehicles: " + map.getVehiclesSize()); // Number of vehicles
        customersLabel.setText("Customers: " + map.getCustomersSize()); // Number of customers
        benchmarkLabel.setText("Benchmark: " + map.getBenchmark());
        saveButton.setVisible(false);
        appendButton.setVisible(false);

        if (!initialized) {
            initializeMapSelector();
        }
    }

    private void initializeMapSelector() {
        ClassLoader classLoader = getClass().getClassLoader();
        File folder = new File(Objects.requireNonNull(classLoader.getResource("resources/maps")).getFile());
        File[] mapFiles = folder.listFiles();
        Arrays.sort(Objects.requireNonNull(mapFiles));

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
        if (ga.getAlphaSolution() != null) {
            durationLabel.setText("Duration: " + Utils.round(ga.getAlphaDuration(), 2));
            fitnessLabel.setText("Fitness: " + Utils.round(ga.getAlphaFitness(), 2));
        }
    }

    /**
     * Toggles paused state on button click
     */
    @FXML
    private void togglePaused() {
        paused = !paused;

        if (paused) {
            startButton.setText("Start");
            saveButton.setVisible(true);
            appendButton.setVisible(true);
            mapSelector.setVisible(true);
        } else {
            startButton.setText("Pause");
            saveButton.setVisible(false);
            appendButton.setVisible(false);
            mapSelector.setVisible(false);
        }
    }

    @FXML
    private void selectMap() {
        fileName = mapSelector.getValue().toString();
        reset();
    }

    public void reset() {
        paused = true;
        durationLabel.setStyle("-fx-font-weight: normal");
        ga = null;
        durationLabel.setText("Duration: 0");
        fitnessLabel.setText("Fitness: 0");
        timeLabel.setText("Time: 0");
        generationLabel.setText("Generation: 0");
        startButton.setVisible(true);
        startButton.setText("Start");
        mapSelector.setVisible(true);
        lineChart.getData().removeAll(seriesAlphaSolution);
        lineChart.getData().removeAll(seriesPopulation);
        initialize();
    }

    private void initializeLineChart() {
        alphaSolutionFitnessData = new HashMap<>();
        populationFitnessData = new HashMap<>();

        seriesAlphaSolution = new XYChart.Series();
        seriesAlphaSolution.setName("Alpha solution");
        seriesPopulation = new XYChart.Series();
        seriesPopulation.setName("Population average");

        lineChart.setCreateSymbols(false);
        lineChart.setAnimated(false);
        lineChart.setHorizontalGridLinesVisible(true);
        lineChart.getXAxis().setAutoRanging(false);
        lineChart.getYAxis().setAutoRanging(false);
        lineChart.getData().setAll(seriesAlphaSolution, seriesPopulation);
    }

    private void updateLinechart() {
        if (!shouldUpdate) return;
        shouldUpdate = false;

        alphaSolutionFitnessData.forEach((key, value) -> {
            seriesAlphaSolution.getData().add(new XYChart.Data<>(key, value));
        });

        populationFitnessData.forEach((key, value) -> {
            seriesPopulation.getData().add(new XYChart.Data<>(key, value));
        });

        xAxis.setLowerBound(0);
        xAxis.setUpperBound(ga.getGeneration());
        xAxis.setTickUnit(ga.getGeneration() / 10);
        yAxis.setLowerBound(map.getBenchmark());
        yAxis.setUpperBound(map.getBenchmark() * 1.50);
        yAxis.setTickUnit(map.getBenchmark() / 10);
        lineChart.getData().removeAll(seriesAlphaSolution);
        lineChart.getData().addAll(seriesAlphaSolution);
    }

    @FXML
    private void save() {
        try {
            ga.saveAlphaSolutionToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void appendToCsv() {
        try {
            ga.appendAlphaSolutionToCsv();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
