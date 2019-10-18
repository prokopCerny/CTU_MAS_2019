package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * column is called x
 * rows are called y
 */
public class Map {
    public final List<MapNode> map;
    private Position depot = null;
    public final List<Position> agents;
    public final int width;
    public final int height;

    public Map(int width, int height) {
        map = new ArrayList<>(width*height);
        this.width = width;
        this.height = height;
        agents = new ArrayList<>(4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                MapNode current = new MapNode(x, y);
                map.add(current);
                if (x > 0) {
                    MapNode n = this.getAt(x-1, y);
                    n.addNeighbor(current);
                    current.addNeighbor(n);
                }
                if (y > 0) {
                    MapNode n = this.getAt(x, y-1);
                    n.addNeighbor(current);
                    current.addNeighbor(n);
                }
            }
        }
    }


    public boolean update(int x, int y, int type, long time) {
        final MapNode n = this.getAt(x, y);
        n.lastSeen = time;
        return n.setType(type);
    }

    public boolean update(final MapMessage m, long time) {
        return this.update(m.x, m.y, m.type, time);
    }

    public MapNode getAt(int x, int y) {
        return map.get(y*width+x);
    }

    public Optional<Position> getDepot() {
        if (depot != null) {
            map.stream().filter(mn -> mn.type == StatusMessage.DEPOT)
                    .findAny()
                    .ifPresent(mn -> {this.depot = new Position(mn.x, mn.y);});
        }
        return Optional.ofNullable(depot);
    }

    public Position goTo(int x, int y) {
        return null;
    }

    public Position oldestClosest(int x, int y) {
        Comparator<MapNode> c = Comparator.comparingLong(n -> n.lastSeen).thenComparingInt(n -> MapUtils.manhattanDist(x, y, n.x, n.y));
        Optional<MapNode> oldClo = map.stream().sorted(c).findAny();
        if (oldClo.isPresent()) {
            return new Position(oldClo.get().x, oldClo.get().y);
        } else {
            oldClo.get();
            return null;
        }
    }
}
