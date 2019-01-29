import MapObjects.Customer;
import MapObjects.Depot;
import MapObjects.Vehicle;
import Utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        Vehicle v1 = new Vehicle(new Depot(5, 5, 5));
        Customer c1 = new Customer(5, 5, 5, 5, 5);
        v1.addCustomer(c1);

        List<Vehicle> l1 = new ArrayList<>();
        l1.add(v1);
        List<Vehicle> l2 = new ArrayList<>(l1);
        System.out.println(l1.get(0).getRoute().get(0).getX());
        l1.get(0).getRoute().remove(0);
        System.out.println(l2.get(0).getRoute().get(0).getX()); // ERROR
    }
}
