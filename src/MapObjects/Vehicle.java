package MapObjects;

import Main.Utils;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

// TODO: Move GA to separate class
public class Vehicle extends MapObject {
    private Depot depot;
    private boolean checkedIn;
    private List<Customer> chromosome;

    // TODO: 1. Implement
    // TODO: 2. Evolve over generations
    private int numberOfSplits;

    public Vehicle(Depot depot, List<Customer> chromosome) {
        super(depot.getX(), depot.getY());
        this.depot = depot;
        this.chromosome = chromosome;
    }

    @Override
    public void tick() {

    }

    /**
     * Renders the chromosome path
     * @param gc
     */
    @Override
    public void render(GraphicsContext gc) {
        gc.setStroke(this.depot.getColor());

        for (int i = 0; i < this.chromosome.size() - 1; i++) {
            Customer gene = this.chromosome.get(i);
            Customer nextGene = this.chromosome.get(i+1);

            gc.strokeLine(gene.getPixelX(), gene.getPixelY(), nextGene.getPixelX(), nextGene.getPixelY());
        }
    }

    public List<Customer> getChromosome() {
        return chromosome;
    }

    /**
     * Shuffles chromosome randomly
     *
     * @param chromosome
     */
    private void setChromosome(List<Customer> chromosome) {
        this.chromosome = chromosome;
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    /**
     * Shuffles customer list to a random chromosome
     */
    public void generateInitialChromosome() {
        Collections.shuffle(this.chromosome);
    }

    /**
     * Calculates total distance for this.chromosome
     * @return
     */
    public double calculateChoromosomeDistance() {
        double totalDistance = 0.0;

        for (int i = 0; i < this.chromosome.size(); i++) {
            totalDistance += this.chromosome.get(i).distance(this.chromosome.get(i + 1));
        }
        return totalDistance;
    }

    /**
     * Mixes n new chromosomes by using this.chromosome with a different chromosome
     *
     * @param otherChromosome TODO: @param numberOfCrossOvers
     * @return
     */
    public List<Customer> crossOver(List<Customer> otherChromosome, int numberOfCrossOvers) {
        final List<Customer>[] DNA = split(this.chromosome, numberOfSplits);
        final List<Customer>[] otherDNA = split(otherChromosome, numberOfSplits);
        List<Customer> firstCrossOver = merge(DNA, otherDNA, 0, numberOfSplits);
        List<Customer> secondCrossOver = merge(otherDNA, DNA, 1, numberOfSplits);

        if (firstCrossOver.size() > this.chromosome.size()) {
            throw new RuntimeException("First crossover is too big!");
        }
        if (secondCrossOver.size() > this.chromosome.size()) {
            throw new RuntimeException("Second crossover is too big!");
        }
        if (firstCrossOver.size() < this.chromosome.size()) {
            throw new RuntimeException("First crossover is too small!");
        }
        if (secondCrossOver.size() < this.chromosome.size()) {
            throw new RuntimeException("Second crossover is too small!");
        }

        List<Customer> mergedCrossOvers = new ArrayList<>();
        mergedCrossOvers.addAll(firstCrossOver);
        mergedCrossOvers.addAll(secondCrossOver);

        return mergedCrossOvers;
    }

    /**
     * Splits chromosome in n parts
     *
     * @param chromosome TODO: @param numberOfSplits
     * @return
     */
    private List<Customer>[] split(List<Customer> chromosome, int numberOfSplits) {
        List<Customer> first = new ArrayList<>();
        List<Customer> second = new ArrayList<>();
        int size = chromosome.size();
        int partitionIndex = Utils.randomIndex(chromosome.size());

        IntStream.range(0, size).forEach(i -> {
            if (i < (size + 1) / partitionIndex) {
                first.add(chromosome.get(i));
            } else {
                second.add(chromosome.get(i));
            }
        });

        return (List<Customer>[]) new List[]{first, second};
    }

    /**
     * Merges the DNA from two chromosomes to a new chromosome
     *
     * @param DNA
     * @param otherDNA TODO: @param numberOfSplits
     * @return
     */
    private List<Customer> merge(List<Customer>[] DNA, List<Customer>[] otherDNA, int keepIndex, int numberOfSplits) {
        List<Customer> crossOver = new ArrayList<>(DNA[keepIndex]);

        for (int i = 0; i < numberOfSplits; i++) {
            for (Customer gene : otherDNA[i]) {
                if (!crossOver.contains(gene)) {
                    crossOver.add(gene);
                }
            }
        }

        return crossOver;
    }

    /**
     * Swaps two random genes from chromosome
     * @return
     */
    public List<Customer> mutate() {
        List<Customer> newChromosome = new ArrayList<>(this.chromosome);
        int indexA = 0;
        int indexB = 0;

        while (indexA == indexB) {
            indexA = Utils.randomIndex(newChromosome.size());
            indexB = Utils.randomIndex(newChromosome.size());
        }

        Collections.swap(newChromosome, indexA, indexB);

        return newChromosome;
    }
}
