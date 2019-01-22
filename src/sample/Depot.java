package sample;

public class Depot {
    private int maxDistance; // D: maximum duration of a route
    private int maxLoad; // Q: allowed maximum load of a vehicle
    private int maxCars; // m: maximum number of vehicles available in each depot
    private int x;
    private int y;

    public Depot(int maxDistance, int maxLoad, int maxCars) {
        this.maxDistance = maxDistance;
        this.maxLoad = maxLoad;
        this.maxCars = maxCars;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public int getMaxLoad() {
        return maxLoad;
    }

    public void setMaxLoad(int maxLoad) {
        this.maxLoad = maxLoad;
    }

    public int getMaxCars() {
        return maxCars;
    }

    public void setMaxCars(int maxCars) {
        this.maxCars = maxCars;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
