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
    private int currentLoad = 0;
    private List<Customer> route = new ArrayList<>();

    public Vehicle(Depot depot) {
        super(depot.getX(), depot.getY());
        this.startDepot = depot;
        this.endDepot = depot;
    }

    public Vehicle(Depot depot, List<Customer> route) {
        super(depot.getX(), depot.getY());
        this.startDepot = depot;
        this.endDepot = depot;
        this.route = route;
        for (Customer customer : route) {
            this.currentLoad += customer.getLoadDemand();
        }
    }

    public Vehicle(Depot startDepot, Depot endDepot, List<Customer> route) {
        super(startDepot.getX(), startDepot.getY());
        this.startDepot = startDepot;
        this.endDepot = endDepot;
        this.route = route;
        for (Customer customer : route) {
            this.currentLoad += customer.getLoadDemand();
        }
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

    public int getMaxLoad() {
        return startDepot.getMaxLoad();
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

    public int getLoadIfCustomerAdded(Customer customer) {
        return currentLoad + customer.getLoadDemand();
    }

    public int getLoadIfRouteAdded(List<Customer> route) {
        return currentLoad + getRouteLoad(route);
    }

    private int getRouteLoad(List<Customer> route) {
        int routeLoad = 0;

        for (Customer customer : route) {
            routeLoad += customer.getLoadDemand();
        }

        return routeLoad;
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

    public double calculateRouteDurationIfCustomerAdded(int index, Customer customerToCheck) {
        if (route.size() == 0) {
            return (startDepot.distance(customerToCheck) + customerToCheck.distance(endDepot));
        }

        double duration = 0.0;
        List<Customer> copy = new ArrayList<>(route);
        copy.add(index, customerToCheck);

        duration += startDepot.distance(copy.get(0));
        duration += copy.get(0).getTimeDemand();
        for (int i = 0; i < copy.size() - 1; i++) {
            duration += copy.get(i).distance(copy.get(i + 1));
            duration += copy.get(i + 1).getTimeDemand();
        }
        duration += copy.get(copy.size() - 1).distance(endDepot);

        return duration;
    }

//    public double calculateRouteDurationIfRouteAdded(int index, List<Customer> routeToCheck) {
//        if (routeToCheck.size() == 0) {
//            return calculateRouteDuration();
//        } else if (route.size() == 0) {
//            return startDepot.distance(routeToCheck.get(0)) + endDepot.distance(routeToCheck.get((routeToCheck.size() - 1)));
//        }
//
//        double duration = 0.0;
//        List<Customer> copy = new ArrayList<>(route);
//        copy.addAll(index, routeToCheck);
//
//        duration += startDepot.distance(copy.get(0));
//        duration += copy.get(0).getTimeDemand();
//        for (int i = 0; i < copy.size() - 1; i++) {
//            duration += copy.get(i).distance(copy.get(i + 1));
//            duration += copy.get(i + 1).getTimeDemand();
//        }
//        duration += copy.get(copy.size() - 1).distance(endDepot);
//
//        return duration;
//    }

    /**
     * Mixes n new routes by using route with a different route
     *
     * @param otherRoute
     * @return
     */
    public List<Customer>[] crossMutate(List<Customer> otherRoute) {
        final List<Customer>[] subRoutes = Utils.splitRoute(route);
        final List<Customer>[] otherSubRoutes = Utils.splitRoute(otherRoute);
        List<Customer> mutatedRoute = merge(subRoutes[0], otherSubRoutes[1]);
        List<Customer> mutatedRoute2 = merge(otherSubRoutes[0], subRoutes[1]);

        return new List[]{mutatedRoute, mutatedRoute2};
    }

    /**
     * Splits route in n parts
     *
     * @param route
     * @return
     */


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
    public List<Customer> swapMutate() {
        if (Controller.verbose) {
            System.out.println("Performing swapMutation on vehicle");
        }
        List<Customer> newRoute = new ArrayList<>(route);

        if (newRoute.size() <= 1) {
            if (Controller.verbose) {
                System.out.println("Route size is zero or one, returning same route");
            }
            return newRoute;
        }

        int indexA = 0;
        int indexB = 0;

        while (indexA == indexB) {
            indexA = Utils.randomIndex(newRoute.size());
            indexB = Utils.randomIndex(newRoute.size());
        }

        Collections.swap(newRoute, indexA, indexB);

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
        for (Customer c : otherRoute) {
            currentLoad += c.getLoadDemand();
        }

        route.addAll(index, otherRoute);
    }

    public boolean smartAddCustomerToRoute(Customer customerToAdd, boolean fitness) {
        double minDuration = Double.MAX_VALUE;
        int minIndex = -1;

        if (currentLoad + customerToAdd.getLoadDemand() > startDepot.getMaxLoad()) {
            return false;
        } else if (route.size() == 0) {
            addCustomerToRoute(customerToAdd);
            return true;
        } else {
            for (int i = 0; i < route.size(); i++) {
                double duration;
//                if (fitness) {
//                     duration = calculateFitnessIfAdded(i, customerToAdd); // TODO Calculate fitnessIfAdded
//                } else {
                duration = calculateRouteDurationIfCustomerAdded(i, customerToAdd);
//                }

                if (duration < minDuration) {
                    minDuration = duration;
                    minIndex = i;
                }
            }

            addCustomerToRoute(minIndex, customerToAdd);
            return true;
        }
    }

    private double calculateFitnessIfAdded(int i, Customer customerToAdd) {
        return 0.0;
    }

    public void removeRouteFromRoute(List<Customer> otherRoute) {
        for (Customer c : otherRoute) {
            currentLoad -= c.getLoadDemand();
        }

        route.removeAll(otherRoute);
    }
}
