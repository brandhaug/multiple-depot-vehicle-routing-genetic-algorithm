package Utils;

import java.util.Random;

public class Utils {

    /**
     * @param limit
     * @return A random int [0, limit>
     */
    public static int randomIndex(int limit) {
        Random random = new Random();
        return limit == 0 ? 0 : random.nextInt(limit);
    }

    /**
     * @return A random double [0, 1]
     */
    public static double randomDouble() {
        Random random = new Random();
        return random.nextDouble();
    }

    /**
     * @param x1 is point1.x
     * @param x2 is point2.x
     * @param y1 is point1.y
     * @param y2 is point2.y
     * @return Euclidean distance between point1 and point2
     */
    public static double euclideanDistance(int x1, int x2, int y1, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}
