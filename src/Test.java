import MapObjects.Depot;
import Utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        List<Integer> yolo = new ArrayList<>();
        yolo.add(4);
        yolo.add(5);
        numbers.addAll(1, yolo);
        System.out.println(Arrays.toString(numbers.toArray()));
    }
}
