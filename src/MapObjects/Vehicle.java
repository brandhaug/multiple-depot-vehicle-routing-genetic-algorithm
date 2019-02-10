package MapObjects;

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
     * Merges the subRoute from two routes to a new route
     *
     * @param subRoute
     * @param otherSubRoute
     * @return
     */
    private List<Customer> merge(List<Customer> subRoute, List<Customer> otherSubRoute) {
        List<Customer> mergedRoute = new ArrayList<>(subRoute);
        mergedRoute.addAll(otherSubRoute);
        return mergedRoute;
    }

    /**
     * Swaps two random genes from route
     *
     * @return
     */
    public List<Customer> swapMutate() {
        List<Customer> newRoute = new ArrayList<>(route);

        if (newRoute.size() <= 1) {
            return newRoute;
        }

        int indexA = 0;
        int indexB = 0;

        while (indexA == indexB) {
            indexA = Utils.randomIndex(newRoute.size());
            indexB = Utils.randomIndex(newRoute.size());
        }

        Collections.swap(newRoute, indexA, indexB);

        return newRoute;
    }

    public boolean addCustomerToRoute(Customer customer) {
        route.add(customer);
        currentLoad += customer.getLoadDemand();
        return true;
    }

    public void addCustomerToRoute(int index, Customer customer) {
        route.add(index, customer);
        currentLoad += customer.getLoadDemand();
    }

    public void removeCustomerFromRoute(Customer customer) {
        route.remove(customer);
        currentLoad -= customer.getLoadDemand();
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

    public boolean smartAddCustomerToRoute(Customer customerToAdd, boolean force) {
        double minDuration = Double.MAX_VALUE;
        int minIndex = -1;

        if (currentLoad + customerToAdd.getLoadDemand() > startDepot.getMaxLoad() && !force) {
            return false;
        } else if (route.size() == 0) {
            addCustomerToRoute(customerToAdd);
            return true;
        } else {
            for (int i = 0; i < route.size(); i++) {
                double duration = calculateRouteDurationIfCustomerAdded(i, customerToAdd);

                if (duration < minDuration) {
                    minDuration = duration;
                    minIndex = i;
                }
            }

            addCustomerToRoute(minIndex, customerToAdd);
            return true;
        }
    }

    public void removeRouteFromRoute(List<Customer> otherRoute) {
        for (Customer c : otherRoute) {
            currentLoad -= c.getLoadDemand();
        }

        route.removeAll(otherRoute);
    }
}
