package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.util.Random;

public final class Utils {

    public static int manhattanDist(StatusMessage status, Position pos) {
        return manhattanDist(status.agentX, status.agentY, pos.x, pos.y);
    }

    public static int manhattanDist(Position pos1, Position pos2) {
        return manhattanDist(pos1.x, pos1.y, pos2.x, pos2.y);
    }

    public static int manhattanDist(int x1, int y1, int x2, int y2) {
        return Math.abs(x1-x2) + Math.abs(y1 - y2);

    }
    public static <T extends Enum<?>> T randomEnum(Class<T> clazz, Random random){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
}
