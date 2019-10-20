package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapNode {
    public int type;
    public final int x;
    public final int y;
    public List<MapNode> neighbors;
    public long lastSeen = 0L;
    public int claimedBy = -1;

    public MapNode(int x, int y) {
        this.x = x;
        this.y = y;
        this.neighbors = new ArrayList<>();
        this.type = 0;
    }

    public void addNeighbor(MapNode other) {
        this.neighbors.add(other);
    }

    public void removeNeighbor(MapNode other) {
        this.neighbors.remove(other);
    }

    public Position getPosition() {
        return new Position(x, y);
    }

    /**
     * sets node's type if type is different and it isn't type agent
     * @param type
     * @return boolean whether something was changed.
     */
    public boolean setType(final int type) {
        if (type == this.type) {
            return false;
        } else {
            switch (type) {
                case StatusMessage.AGENT:
                    return false;
                case StatusMessage.OBSTACLE:
                    this.neighbors.forEach(n -> n.removeNeighbor(this));
                    this.neighbors.clear();
                case StatusMessage.DEPOT:
                case StatusMessage.GOLD:
                    this.type = type;
                    return true;
                default:
                    throw new IllegalStateException("Unexpected MapNode type: " + type);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapNode mapNode = (MapNode) o;
        return x == mapNode.x &&
                       y == mapNode.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
