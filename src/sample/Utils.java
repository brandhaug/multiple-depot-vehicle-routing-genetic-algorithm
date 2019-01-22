package sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public void parseMapFile(String path) throws IOException {
        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        int index = 0;
        int depotIndex = 0;

        int maxVehicles = 0; // m: maximum number of vehicles available in each depot
        int totalCustomers = 0; // n: total number of customers
        int depotsCount = 0; // t: number of depots

        int maxDistance = 0; // D: maximum duration of a route
        int maxLoad = 0; // Q: allowed maximum load of a vehicle

        List<Customer> customerList = new ArrayList<>();
        List<Depot> depotList = new ArrayList<>();

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
                depotList.add(depot);
            } else if (index <= depotsCount + totalCustomers) { // id, x, y, d, q
                System.out.println("Customer info");
                Customer customer = new Customer(lineArr[0], lineArr[1], lineArr[2], lineArr[3], lineArr[4]);
                customerList.add(customer);
            } else if (depotIndex <= depotsCount) { // id, x, y
                System.out.println("Depot location");
                depotList.get(depotIndex).setCoordinates(lineArr[1], lineArr[2]);
                depotIndex++;
            } else {
                System.out.println("Oh no, I shouldn't be here!");
            }

            System.out.println(line);
            index++;
        }

        // TODO: Return Object lists
    }
}
