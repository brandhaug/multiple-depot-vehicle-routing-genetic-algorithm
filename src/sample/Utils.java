package sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Utils {

    public void parseMapFile(String path) throws IOException {
        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        int index = 0;

        int maxVehicles = 0; // m: maximum number of vehicles available in each depot
        int totalCustomers = 0; // n: total number of customers
        int depotsCount = 0; // t: number of depots

        int maxDistance = 0; // D: maximum duration of a route
        int maxLoad = 0; // Q: allowed maximum load of a vehicle

        int customerNumber = 0; // i: customer number
        int customerX = 0;
        int customerY = 0;
        int customerTime = 0; // d: necessary service duration required for this customer
        int customerDemand = 0; // q: demand for this customer

        int depotX = 0;
        int depotY = 0;

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
                maxDistance = lineArr[0];
                maxLoad = lineArr[1];
            } else if (index <= (depotsCount + totalCustomers)) {
                System.out.println("Customer info");
                customerNumber = lineArr[0];
                customerX = lineArr[1];
                customerY = lineArr[2];
                customerTime = lineArr[3];
                customerDemand = lineArr[4];
            } else {
                System.out.println("Depot location");
                depotX = lineArr[1];
                depotY = lineArr[2];
            }

            System.out.println(line);
            index++;
        }

    }
}
