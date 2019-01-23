package Main;

import MapObjects.Customer;
import MapObjects.Depot;
import MapObjects.Vehicle;
import javafx.scene.canvas.GraphicsContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Map {
    private List<Depot> depots;
    private List<Customer> customers;
    private List<Vehicle> vehicles;
    private int maximumX;
    private int maximumY;
    public static int scaleX;
    public static int scaleY;


    public Map(String fileName) throws IOException {
        depots = new ArrayList<>();
        customers = new ArrayList<>();
        vehicles = new ArrayList<>();
        parseMapFile(fileName);
    }


    public void parseMapFile(String fileName) throws IOException {
        File file = new File("./assets/maps/" + fileName);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        int index = 0;
        int depotIndex = 0;

        int maxVehicles = 0; // m: maximum number of vehicles available in each depot
        int totalCustomers = 0; // n: total number of customers
        int depotsCount = 0; // t: number of depots

        int maxDistance = 0; // D: maximum duration of a route
        int maxLoad = 0; // Q: allowed maximum load of a vehicle


        while ((line = br.readLine()) != null) {
            String[] stringLineArr = line.trim().split("\\s+");
            int[] lineArr = Arrays.stream(stringLineArr).mapToInt(Integer::parseInt).toArray();

            if (index == 0) { // First line contains the following information: m n t
                System.out.println("Map info");
                maxVehicles = lineArr[0];
                totalCustomers = lineArr[1];
                depotsCount = lineArr[2];
            } else if (index <= depotsCount) { // The next t lines contain, the following information: D Q
                System.out.println("Depot info");
                Depot depot = new Depot(lineArr[0], lineArr[1], maxVehicles);
                depots.add(depot);
            } else if (index <= depotsCount + totalCustomers) { // id, x, y, d, q
                System.out.println("Customer info");
                Customer customer = new Customer(lineArr[0], lineArr[1], lineArr[2], lineArr[3], lineArr[4]);
                customers.add(customer);
                maximumX = Math.max(lineArr[1], maximumX);
                maximumY = Math.max(lineArr[2], maximumY);
            } else if (depotIndex <= depotsCount) { // id, x, y
                System.out.println("Depot location");
                Depot depot = depots.get(depotIndex);
                depot.setCoordinates(lineArr[1], lineArr[2]);
                maximumX = Math.max(lineArr[1], maximumX);
                maximumY = Math.max(lineArr[2], maximumY);

                for (int i = 0; i < maxVehicles; i++) {
                    Vehicle vehicle = new Vehicle(depot);
                    vehicles.add(vehicle); // TODO: Add maxDistance and maxLoad
                }

                depotIndex++;
            } else {
                System.out.println("Oh no, I shouldn't be here!");
            }

            System.out.println(line);
            index++;
        }

        scaleX = (Controller.CANVAS_WIDTH - Controller.CANVAS_MARGIN) / maximumX;
        scaleY = (Controller.CANVAS_HEIGHT - Controller.CANVAS_MARGIN)  / maximumY;
    }

    public void tick() {

    }

    public void render(GraphicsContext gc) {
        renderDepots(gc);
        renderCustomers(gc);
        renderVehicles(gc);
    }

    private void renderDepots(GraphicsContext gc) {
        for (Depot depot : depots) {
            depot.render(gc);
        }
    }

    private void renderCustomers(GraphicsContext gc) {
        for (Customer customer : customers) {
            customer.render(gc);
        }
    }

    private void renderVehicles(GraphicsContext gc) {
        for (Vehicle vehicle : vehicles) {
            vehicle.render(gc);
        }
    }
}
