package GeneticAlgorithm;

import MapObjects.Depot;
import MapObjects.Vehicle;
import javafx.scene.canvas.GraphicsContext;

import java.io.IOException;
import java.util.List;

/**
 * Main method for GeneticAlgorithm
 * GeneticAlgorithm contains one Population.
 * Population contains n Solutions. n = populationSize
 * Sends all necessary data about GA/population to Controller
 */
public class GeneticAlgorithm {

    // Parameters
    private int populationSize = 200; // 20-100 dependent on problem
    private double crossOverRate = 0.8; // 80%-95%
    private double mutationRate = 0.9; // 0.5%-1%.
    private double selectionRate = 0.2;
    private int tournamentSize = 3;
    private int numberOfChildren = populationSize/3;

    private List<Depot> depots;
    private Population population;

    /**
     * Creates initial population
     * @param depots
     */
    public GeneticAlgorithm(List<Depot> depots) {
        this.depots = depots;
        population = new Population(depots, populationSize, crossOverRate, mutationRate, selectionRate, tournamentSize, numberOfChildren);
    }

    /**
     * Functionality loop
     * One loop = one generation
     * population.tick() loops through all solutions
     */
    public void tick() {
        population.tick();
    }

    /**
     * Draws solution in canvas
     * @param gc
     */
    public void render(GraphicsContext gc) {
        renderAlphaSolution(gc);
    }

    /**
     * Draws best solution in canvas
     * @param gc
     */
    private void renderAlphaSolution(GraphicsContext gc) {
        Solution alphaSolution = getAlphaSolution();

        if (alphaSolution != null) {
            for (Vehicle vehicle : alphaSolution.getVehicles()) {
                vehicle.render(gc);
            }
        }
    }

    /**
     * Get best fitness of Population
     */
    public double getAlphaFitness() {
        return population.getAlphaFitness();
    }

    /**
     * Get best Solution (Solution with best fitness) of Population
     */
    public Solution getAlphaSolution() {
        return population.getAlphaSolution();
    }

    public int getGeneration() {
        return population.getGeneration();
    }

    public void saveAlphaSolutionToFile() throws IOException {
        getAlphaSolution().saveToFile();
    }
}
