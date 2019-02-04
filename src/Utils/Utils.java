package Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    /**
     * https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
     * @param value
     * @param places
     * @return
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
