package student;

import java.util.Random;

public final class Utils {
    public static int manhattanDist(int x1, int y1, int x2, int y2) {
        return Math.abs(x1-x2) + Math.abs(y1 - y2);

    }
    public static <T extends Enum<?>> T randomEnum(Class<T> clazz, Random random){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
}
