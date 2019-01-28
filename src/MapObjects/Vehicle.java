package MapObjects;

import Main.Controller;
import Utils.Utils;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class Vehicle extends MapObject {
    private Depot depot;
    private List<Customer> route = new ArrayList<>();

    // TODO: 1. Implement
    // TODO: 2. Evolve over generations
    private int numberOfSplits;

    public Vehicle(Depot depot, List<Customer> route) {
        super(depot.getX(), depot.getY());
        this.depot = depot;
        if (route != null) {
            this.route = route;
        }
    }

    @Override
    public void tick() {

    }

    /**
     * Renders the route path
     *
     * @param gc
     */
    @Override
    public void render(GraphicsContext gc) {
        if (this.route.size() > 0) {
            gc.setStroke(this.depot.getColor());
            gc.strokeLine(depot.getPixelX(), depot.getPixelY(), this.route.get(0).getPixelX(), this.route.get(0).getPixelY());

            for (int i = 0; i < this.route.size() - 1; i++) {
                Customer gene = this.route.get(i);
                Customer nextGene = this.route.get(i + 1);

                gc.strokeLine(gene.getPixelX(), gene.getPixelY(), nextGene.getPixelX(), nextGene.getPixelY());
            }

            gc.strokeLine(this.route.get(this.route.size() - 1).getPixelX(), this.route.get(this.route.size() - 1).getPixelY(), this.depot.getPixelX(), this.depot.getPixelY());
        }
    }

    public List<Customer> getRoute() {
        return route;
    }

    /**
     * Shuffles route randomly
     *
     * @param route
     */
    private void setRoute(List<Customer> route) {
        this.route = route;
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    /**
     * Calculates total distance for this.route
     *
     * @return
     */
    public double calculateRouteDistance() {
        double routeDistance = 0.0;

        for (int i = 0; i < this.route.size() - 1; i++) {
            routeDistance += this.route.get(i).distance(this.route.get(i + 1));
        }
        return routeDistance;
    }

    /**
     * Mixes n new routes by using this.route with a different route
     *
     * @param otherRoute TODO: @param numberOfCrossOvers
     * @return
     */
    public List<Customer> crossOver(List<Customer> otherRoute, int numberOfCrossOvers) {
        final List<Customer>[] subRoutes = split(this.route, numberOfSplits);
        final List<Customer>[] otherSubRoutes = split(otherRoute, numberOfSplits);
        List<Customer> firstCrossOver = merge(subRoutes, otherSubRoutes, 0, numberOfSplits);
        List<Customer> secondCrossOver = merge(otherSubRoutes, subRoutes, 1, numberOfSplits);

        if (Controller.verbose) {
            System.out.println("Route: " + this.route.toString());
            System.out.println("Other route: " + otherRoute.toString());
            System.out.println("First crossover: " + firstCrossOver.toString());
            System.out.println("Second crossover: " + secondCrossOver.toString());
        }

//        if (firstCrossOver.size() > this.route.size()) {
//            throw new RuntimeException("First crossover is too big!");
//        }
//        if (secondCrossOver.size() > this.route.size()) {
//            throw new RuntimeException("Second crossover is too big!");
//        }
//        if (firstCrossOver.size() < this.route.size()) {
//            throw new RuntimeException("First crossover is too small!");
//        }
//        if (secondCrossOver.size() < this.route.size()) {
//            throw new RuntimeException("Second crossover is too small!");
//        }

        List<Customer> mergedCrossOvers = new ArrayList<>();
        mergedCrossOvers.addAll(firstCrossOver);
        mergedCrossOvers.addAll(secondCrossOver);

        return mergedCrossOvers;
    }

    /**
     * Splits route in n parts
     *
     * @param route TODO: @param numberOfSplits
     * @return
     */
    private List<Customer>[] split(List<Customer> route, int numberOfSplits) {
        System.out.println("========= Splitting route to subRoutes =========");
        List<Customer> first = new ArrayList<>();
        List<Customer> second = new ArrayList<>();
        int size = route.size();
        int partitionIndex = Utils.randomIndex(size) + 1;
        System.out.println("Partition Index: " + partitionIndex);

        IntStream.range(0, size).forEach(i -> {
            if (i < (size + 1) / partitionIndex) {
                first.add(route.get(i));
            } else {
                second.add(route.get(i));
            }
        });

        System.out.println("First subRoute: " + first.toString());
        System.out.println("Second subRoute: " + second.toString());

        List<Customer>[] splittedRoute = new List[]{first, second};
        System.out.println("========= END Splitting route to subRoutes =========");

        return splittedRoute;
    }

    /**
     * Merges the subRoute from two routes to a new route
     *
     * @param subRoute
     * @param otherSubRoute TODO: @param numberOfSplits
     * @return
     */
    private List<Customer> merge(List<Customer>[] subRoute, List<Customer>[] otherSubRoute, int keepIndex, int numberOfSplits) {
        System.out.println("========= Merging two subRoutes to a route  =========");
        List<Customer> crossOver = new ArrayList<>(subRoute[keepIndex]);

        System.out.println("Initial subRoute: " + crossOver);

        for (int i = 0; i < otherSubRoute.length; i++) {
            for (Customer gene : otherSubRoute[i]) {
                if (!crossOver.contains(gene)) {
                    crossOver.add(gene);
                }
            }
        }

        System.out.println("Merged subRoutes: " + crossOver);
        System.out.println("========= Merging two subRoutes to a route  =========");
        return crossOver;
    }

    /**
     * Swaps two random genes from route
     *
     * @return
     */
    public List<Customer> mutate() {
        List<Customer> newRoute = new ArrayList<>(this.route);
        int indexA = 0;
        int indexB = 0;

        while (indexA == indexB) {
            indexA = Utils.randomIndex(newRoute.size());
            indexB = Utils.randomIndex(newRoute.size());
        }

        System.out.println(newRoute);
        Collections.swap(newRoute, indexA, indexB);
        System.out.println(newRoute);

        return newRoute;
    }

    @Override
    public String toString() {
        return Double.toString(calculateRouteDistance());
    }

    public void addCustomer(Customer customer) {
        this.route.add(customer);
    }

    public void shuffleRoute() {
        Collections.shuffle(this.route);
        if (Controller.verbose) {
            System.out.println("Shuffled route: " + this.route.toString());
        }
    }

    /**
     * Finds the nearest point for each
     */
    public void optimizeRoute() {
        MapObject lastPoint = this.depot;
        List<Customer> newRoute = new ArrayList<>();
        Customer nearestGene = null;

        while (newRoute.size() != this.route.size()) {
            double minimumDistance = Double.MAX_VALUE;
            for (int i = 0; i < this.route.size(); i++) {
                Customer gene = this.route.get(i);
                double distance = Utils.euclideanDistance(lastPoint.getX(), gene.getX(), lastPoint.getY(), gene.getY());

                if (!newRoute.contains(gene) && distance < minimumDistance) {
                    minimumDistance = distance;
                    nearestGene = gene;
                }
            }

            newRoute.add(nearestGene);
            lastPoint = nearestGene;
        }

        this.route = newRoute;
    }
}
