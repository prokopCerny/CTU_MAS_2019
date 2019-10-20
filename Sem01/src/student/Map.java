package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * column is called x
 * rows are called y
 */
public class Map {
    private final Agent agent;
    public final List<MapNode> map;
    private Position depot = null;
    public MapNode[] agents;
    public final int width;
    public final int height;
    public final MapNode[] agentClaims;

    private boolean explored = false;

    public Map(Agent agent, int width, int height) {
        this.agent = agent;
        map = new ArrayList<>(width*height);
        agentClaims = new MapNode[4];
        this.width = width;
        this.height = height;
        agents = new MapNode[4];
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

    public void removeClaim(int x, int y, int agentId) {
        agentClaims[agentId-1] = null;
        MapNode n = getAt(x, y);
        if (n.claimedBy != -1) {
            agentClaims[n.claimedBy - 1] = null;
        }
        n.claimedBy = -1;
        n.type = 0;
    }

    public int updateClaim(int x, int y, int agentId) {
        MapNode n = getAt(x, y);
        if (n.claimedBy == -1) {
            n.claimedBy = agentId;
            agentClaims[agentId-1] = n;
            return agentId;
        } else {
            if (n.claimedBy < agentId) {
                return n.claimedBy;
            } else {
                agentClaims[n.claimedBy-1] = null;
                n.claimedBy = agentId;
                agentClaims[agentId-1] = n;
                return agentId;
            }
        }

    }


    public boolean update(int x, int y, int type, long time) {
        final MapNode n = this.getAt(x, y);
        n.lastSeen = time;
        return n.setType(type);
    }

    public void updateNeighborhoodTime(StatusMessage status, long time) {
        updateNeighborhoodTime(status.agentX, status.agentY, time);
    }

    public void updateNeighborhoodTime(int x, int y, long time) {
        for (int x_off = -1; x_off <= 1; x_off++) {
            for (int y_off = -1; y_off <= 1; y_off++) {
                int curX = x + x_off;
                int curY = y + y_off;
                if ((curX >= 0) && (curX < width) && (curY >= 0) && (curY < height) ) {
                    this.getAt(curX, curY).lastSeen = time;
                }
            }
        }
    }

    public boolean update(final MapMessage m, long time) {
        return this.update(m.x, m.y, m.type, time);
    }

    public int getIndex(MapNode n) {
        return getIndex(n.x, n.y);
    }

    public int getIndex(int x, int y) {
        return y*width+x;
    }
    public MapNode getAt(int x, int y) {
        return map.get(getIndex(x, y));
    }

    public MapNode getAt(Position pos) {
        return getAt(pos.x, pos.y);
    }

    public Optional<Position> getDepot() {
        if (depot == null) {
            map.stream().filter(mn -> mn.type == StatusMessage.DEPOT)
                    .findAny()
                    .ifPresent(mn -> {this.depot = new Position(mn.x, mn.y);});
        }
        return Optional.ofNullable(depot);
    }

    public Position goFromTo(StatusMessage from, Position to, int maxManhattanDist) {
        return goFromTo(from.agentX, from.agentY, to.x, to.y, maxManhattanDist);
    }

    public Position goFromTo(StatusMessage from, Position to) {
        return goFromTo(from, to, 0);
    }

    public Position goFromTo(int x0, int y0, int x, int y) {
        return goFromTo(x0, y0, x, y, 0);
    }

    public Position goFromTo(int x0, int y0, int x, int y, int maxManhattanDist) {
        if (Utils.manhattanDist(x0, y0, x, y) <= maxManhattanDist) {
//            try {
//                agent.log("Very close");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            return null;
        }
        MapNode start = getAt(x0, y0);
        Queue<MapNode> queue = new LinkedList<>();
        queue.add(start);
        int[] parent = new int[width*height];
        boolean[] visited = new boolean[width*height];
        parent[getIndex(start)] = -1;
        visited[getIndex(start)] = true;
        while (!queue.isEmpty()){
            MapNode cur = queue.remove();
            if (Utils.manhattanDist(cur.x, cur.y, x, y) <= maxManhattanDist) {
                int end = getIndex(cur);
                while (parent[end] != getIndex(start)) {
                    end = parent[end];
                }
                MapNode neighborToGoTo = map.get(end);
                return new Position(neighborToGoTo.x, neighborToGoTo.y);
            }
            for (MapNode n : cur.neighbors) {
                if (!visited[getIndex(n)]){
                    parent[getIndex(n)] = getIndex(cur);
                    visited[getIndex(n)] = true;
                    if (Utils.manhattanDist(x0, y0, n.x, n.y) > 2 || Arrays.stream(agents).filter(Objects::nonNull).noneMatch(mn -> mn.x == n.x && mn.y == n.y)) {
                        queue.add(n);
                    }
                }
            }
        }
        return null;
    }

    public Position oldestClosest(StatusMessage from) {
        return oldestClosest(from.agentX, from.agentY);
    }

    public Position oldestClosest(int x, int y) {
//        Comparator<MapNode> c = Comparator.<MapNode>comparingLong(n -> n.lastSeen).thenComparing(n -> Utils.manhattanDist(x, y, n.x, n.y), Collections.reverseOrder());
        Comparator<MapNode> c = Comparator.<MapNode>comparingLong(n -> n.lastSeen).thenComparing(n -> Utils.manhattanDist(x, y, n.x, n.y));
        Optional<MapNode> oldClo = map.stream().filter(n -> n.type != StatusMessage.OBSTACLE).sorted(c).findAny();

        if (oldClo.isPresent()) {
            try {
                agent.log(String.format("Oldest closest (%d, %d)", oldClo.get().x, oldClo.get().y));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new Position(oldClo.get().x, oldClo.get().y);
        } else {
            oldClo.get();
            return null;
        }
    }

    public boolean isExplored() {
        if (!explored) {
            explored = map.stream().noneMatch(n -> n.lastSeen == 0L);
        }
        return explored;
    }

    public List<Integer> orderNearestOtherAgents(int x, int y) {
        List<Integer> nearestAgents = IntStream.range(0, 4).boxed()
            .filter(i -> i != agent.getAgentId()-1)
            .filter(i -> Objects.nonNull(agents[i]))
            .sorted(Comparator.comparingInt(i -> Utils.manhattanDist(agents[ i].x, agents[i].y, x, y)))
            .map(i -> i+1)
            .collect(Collectors.toList());
        return nearestAgents;
    }
}
