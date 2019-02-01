package GeneticAlgorithm;

import MapObjects.Depot;
import MapObjects.Vehicle;
import javafx.scene.canvas.GraphicsContext;

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
    private double mutationRate = 0.1; // 0.5%-1%.
    private double selectionRate = 0.2;


    // Lists
    private List<Depot> depots;

    private Population population;

    /**
     * Creates initial population
     *
     * @param depots
     */
    public GeneticAlgorithm(List<Depot> depots) {
        this.depots = depots;
        population = new Population(depots, populationSize, crossOverRate, mutationRate, selectionRate);
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
        Solution solution = getAlphaSolution();
        for (Vehicle vehicle : solution.getVehicles()) {
            vehicle.render(gc);
        }
    }

    /**
     * Get best fitness of Population
     */
    public int getAlphaFitness() {
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

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public double getCrossOverRate() {
        return crossOverRate;
    }

    public void setCrossOverRate(double crossOverRate) {
        this.crossOverRate = crossOverRate;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    public List<Depot> getDepots() {
        return depots;
    }

    public void setDepots(List<Depot> depots) {
        this.depots = depots;
    }

    public Population getPopulation() {
        return population;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }
}
