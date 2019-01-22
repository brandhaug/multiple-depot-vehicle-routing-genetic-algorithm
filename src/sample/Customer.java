package sample;

public class Customer {
    private int id; // i: customer number
    private int x;
    private int y;
    private int serviceDuration; // d: necessary service duration required for this customer
    private int loadDemand; // q: demand for this customer

    public Customer(int id, int x, int y, int serviceDuration, int loadDemand) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.serviceDuration = serviceDuration;
        this.loadDemand = loadDemand;
    }
}
