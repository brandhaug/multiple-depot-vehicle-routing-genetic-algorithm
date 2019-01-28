package GeneticAlgorithm;

import MapObjects.Depot;
import MapObjects.Vehicle;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

public class GeneticAlgorithm {

    // Parameters
    private int populationSize = 50; // 20-100 dependent on problem
    private double crossOverRate = 0.8; // 80%-95%
    private double mutationRate = 0.05; // 0.5%-1%.
    private double selectionRate = 0.2; // 0.5%-1%.
    private int generation = 0;

    // Lists
    private List<Depot> depots;
    private List<Vehicle> vehicles;

    private Population population;

    public GeneticAlgorithm(List<Depot> depots, List<Vehicle> vehicles) {
        this.depots = depots;
        this.vehicles = vehicles;
        population = new Population(depots, populationSize, crossOverRate, mutationRate, selectionRate);
    }

    public void tick() {
        population.tick();
        generation++;
    }

    public void render(GraphicsContext gc) {
        renderAlphaSolution(gc);
    }

    private void renderAlphaSolution(GraphicsContext gc) {
        Solution solution = getAlphaSolution();
        for (Vehicle vehicle : solution.getVehicles()) {
            vehicle.render(gc);
        }
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

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public List<Depot> getDepots() {
        return depots;
    }

    public void setDepots(List<Depot> depots) {
        this.depots = depots;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public Population getPopulation() {
        return population;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }

    public int getAlphaFitness() {
        return population.getAlphaFitness();
    }

    public Solution getAlphaSolution() {
        return population.getAlphaSolution();
    }
}
