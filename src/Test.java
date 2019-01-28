import MapObjects.Depot;

public class Test {
    public static void main(String[] args) {
        Depot depot = new Depot(10, 5, 4);
        Depot depot2 = new Depot(depot.getMaxDistance(), depot.getMaxLoad(), depot.getMaxCars());
        depot2.setMaxDistance(15);
        System.out.println(depot.getMaxDistance());
    }
}
