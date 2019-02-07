package MapObjects;

import Main.Controller;
import Utils.Utils;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Vehicle extends MapObject {
    private Depot startDepot;
    private Depot endDepot;
    private int currentLoad;
    private List<Customer> route = new ArrayList<>();

    public Vehicle(Depot depot) {
        super(depot.getX(), depot.getY());
        this.startDepot = depot;
        this.endDepot = depot;
        this.currentLoad = 0;
    }

    public Vehicle(Depot depot, List<Customer> route) {
        super(depot.getX(), depot.getY());
        this.startDepot = depot;
        this.endDepot = depot;
        this.route = route;
        this.currentLoad = 0;
    }

    public Vehicle(Depot startDepot, Depot endDepot, List<Customer> route) {
        super(startDepot.getX(), startDepot.getY());
        this.startDepot = startDepot;
        this.endDepot = endDepot;
        this.route = route;
        this.currentLoad = 0;
    }

    /**
     * Renders the route path
     *
     * @param gc
     */
    @Override
    public void render(GraphicsContext gc) {
        if (route.size() > 0) {
            gc.setStroke(startDepot.getColor());
            gc.strokeLine(startDepot.getPixelX(), startDepot.getPixelY(), route.get(0).getPixelX(), route.get(0).getPixelY());

            for (int i = 0; i < route.size() - 1; i++) {
                Customer gene = route.get(i);
                Customer nextGene = route.get(i + 1);

                gc.strokeLine(gene.getPixelX(), gene.getPixelY(), nextGene.getPixelX(), nextGene.getPixelY());
            }

            gc.strokeLine(route.get(route.size() - 1).getPixelX(), route.get(route.size() - 1).getPixelY(), endDepot.getPixelX(), endDepot.getPixelY());
        }
    }

    public List<Customer> getRoute() {
        return route;
    }

    public Depot getStartDepot() {
        return startDepot;
    }

    public Depot getEndDepot() {
        return endDepot;
    }

    public void setEndDepot(Depot depot) {
        endDepot = depot;
    }

    public int getCurrentLoad() {
        return currentLoad;
    }

    /**
     * Calculates total distance for route
     *
     * @return
     */
    public double calculateRouteDistance() {
        double routeDistance = 0.0;

        if (route.size() == 0) {
            return routeDistance;
        }

        routeDistance += startDepot.distance(route.get(0));
        for (int i = 0; i < route.size() - 1; i++) {
            routeDistance += route.get(i).distance(route.get(i + 1));
        }
        routeDistance += route.get(route.size() - 1).distance(endDepot);
        return routeDistance;
    }

    /**
     * Calculates total distance for route
     *
     * @return
     */
    public double calculateRouteDuration() {
        double routeDistance = 0.0;

        if (route.size() == 0) {
            return routeDistance;
        }

        routeDistance += startDepot.distance(route.get(0));
        for (int i = 0; i < route.size() - 1; i++) {
            routeDistance += route.get(i).getTimeDemand();
            routeDistance += route.get(i).distance(route.get(i + 1));
        }
        routeDistance += route.get(route.size() - 1).distance(endDepot);
        return routeDistance;
    }

    public double calculateRouteDuration(int index, Customer customerToCheck) {
        if (route.size() == 0) {
            return (startDepot.distance(customerToCheck) + customerToCheck.distance(endDepot));
        }

        double distance = 0.0;
        List<Customer> copy = new ArrayList<>(route);
        copy.add(index, customerToCheck);

        distance += startDepot.distance(copy.get(0));
        for (int i = 0; i < copy.size() - 1; i++) {
            distance += copy.get(i).distance(copy.get(i + 1));
        }
        distance += copy.get(copy.size() - 1).distance(endDepot);
        return distance;
    }

    /**
     * Mixes n new routes by using route with a different route
     *
     * @param otherRoute
     * @return
     */
    public List<Customer>[] mutation2(List<Customer> otherRoute) {
        final List<Customer>[] subRoutes = split(route);
        final List<Customer>[] otherSubRoutes = split(otherRoute);
        List<Customer> firstCrossOver = merge(subRoutes[0], otherSubRoutes[1]);
        List<Customer> secondCrossOver = merge(otherSubRoutes[0], subRoutes[1]);

        if (Controller.verbose) {
            System.out.println("Route: " + route.toString());
            System.out.println("Other route: " + otherRoute.toString());
            System.out.println("First crossover: " + firstCrossOver.toString());
            System.out.println("Second crossover: " + secondCrossOver.toString());
        }

        return new List[]{firstCrossOver, secondCrossOver};
    }

    /**
     * Splits route in n parts
     *
     * @param route
     * @return
     */
    private List<Customer>[] split(List<Customer> route) {
        if (Controller.verbose) {
            System.out.println("========= Splitting route to subRoutes =========");
        }
        List<Customer> first = new ArrayList<>();
        List<Customer> second = new ArrayList<>();
        int size = route.size();

        if (size != 0) {
            int partitionIndex = Utils.randomIndex(size);

            if (Controller.verbose) {
                System.out.println("Partition Index: " + partitionIndex);
            }

            for (int i = 0; i < route.size(); i++) {
                if (partitionIndex > i) {
                    first.add(route.get(i));
                } else {
                    second.add(route.get(i));
                }
            }
        }
        if (Controller.verbose) {
            System.out.println("First subRoute: " + first.toString());
            System.out.println("Second subRoute: " + second.toString());
        }

        if (Controller.verbose) {
            System.out.println("========= END Splitting route to subRoutes =========");
        }

        return new List[]{first, second};
    }

    /**
     * Merges the subRoute from two routes to a new route
     *
     * @param subRoute
     * @param otherSubRoute
     * @return
     */
    private List<Customer> merge(List<Customer> subRoute, List<Customer> otherSubRoute) {
        if (Controller.verbose) {
            System.out.println("========= Merging two subRoutes to a route  =========");
        }
        List<Customer> crossOver = new ArrayList<>(subRoute);

        if (Controller.verbose) {
            System.out.println("Initial subRoute: " + crossOver);
        }

        crossOver.addAll(otherSubRoute);


        if (Controller.verbose) {
            System.out.println("Merged subRoutes: " + crossOver);
            System.out.println("========= Merging two subRoutes to a route  =========");
        }
        return optimizeRoute(crossOver);
    }

    /**
     * Swaps two random genes from route
     *
     * @return
     */
    public List<Customer> mutate() {
        if (Controller.verbose) {
            System.out.println("Performing mutation on vehicle");
        }
        List<Customer> newRoute = new ArrayList<>(route);

        if (newRoute.size() <= 1) {
            System.out.println("Route size is zero or one, returning same route");
            return newRoute;
        }

        int indexA = 0;
        int indexB = 0;

        while (indexA == indexB) {
            indexA = Utils.randomIndex(newRoute.size());
            indexB = Utils.randomIndex(newRoute.size());
        }


        //TODO: SHOULD THESE PRINTLN BE REMOVED?
        //System.out.println(newRoute);
        Collections.swap(newRoute, indexA, indexB);
        //System.out.println(newRoute);

        if (Controller.verbose) {
            System.out.println("Mutation on Vehicle finished, returning new Route");
        }
        return newRoute;
    }


    public void addCustomerToRoute(Customer customer) {
        route.add(customer);
        currentLoad += customer.getLoadDemand();
    }

    public void addCustomerToRoute(int index, Customer customer) {
        route.add(index, customer);
        currentLoad += customer.getLoadDemand();
    }

    public void removeCustomerFromRoute(Customer customer) {
        route.remove(customer);
        currentLoad -= customer.getLoadDemand();
    }

    public void shuffleRoute() {
        Collections.shuffle(route);
        if (Controller.verbose) {
            System.out.println("Shuffled route: " + route.toString());
        }
    }

    /**
     * Finds the nearest point for each
     */
    public void optimizeRoute() {
        route = optimizeRoute(route);
    }

    private List<Customer> optimizeRoute(List<Customer> routeToOptimize) {
        MapObject lastPoint = startDepot;
        List<Customer> newRoute = new ArrayList<>();
        Customer nearestGene = null;

        while (newRoute.size() != routeToOptimize.size()) {
            double minimumDistance = Double.MAX_VALUE;
            for (Customer gene : routeToOptimize) {
                double distance = Utils.euclideanDistance(lastPoint.getX(), gene.getX(), lastPoint.getY(), gene.getY());

                if (!newRoute.contains(gene) && distance < minimumDistance) {
                    minimumDistance = distance;
                    nearestGene = gene;
                }
            }

            newRoute.add(nearestGene);
            lastPoint = nearestGene;
        }

        return newRoute;
    }

    @Override
    public Vehicle clone() {
        List<Customer> copyOfRoute = new ArrayList<>(route);
        return new Vehicle(startDepot, endDepot, copyOfRoute);
    }

    public void addOtherRouteToRoute(int index, List<Customer> otherRoute) {
        route.addAll(index, otherRoute);
    }
}
