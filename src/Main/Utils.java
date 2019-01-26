package Main;

import java.util.Random;

public class Utils {

    // TODO: Maybe delete 1 +
    public static int randomIndex(int limit) {
        Random random = new Random();
        return 1 + random.nextInt(limit);
    }
}
