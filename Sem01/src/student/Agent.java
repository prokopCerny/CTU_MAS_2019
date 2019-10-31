package student;

import mas.agents.AbstractAgent;
import mas.agents.Message;
import mas.agents.SimulationApi;
import mas.agents.StringMessage;
import mas.agents.task.mining.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class Agent extends AbstractAgent {
    Strategy strategy = new OldestWalkStrategy(this);
    public Map map;
    public long time = 1L;
    boolean hasGold = false;
    final Random random = new Random(43);

    enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    public Agent(int id, InputStream is, OutputStream os, SimulationApi api) throws IOException, InterruptedException {
        super(id, is, os, api);
    }

    // See also class StatusMessage
    public static String[] types = {
            "", "obstacle", "depot", "gold", "agent"
    };

    @Override
    public void act() throws Exception {
        StatusMessage start = sense();
        map = new Map(this, start.width, start.height);
        map.updateNeighborhoodTime(start, time);
        map.agents[getAgentId()-1] = map.getAt(start.agentX, start.agentY);
        for (int agentId = 1; agentId <= 4; agentId++) {
            if (agentId != getAgentId()) {
                sendMessage(agentId, new MapMessage(StatusMessage.AGENT, start.agentX, start.agentY));
            }
        }
        Thread.sleep(50);


        while (true) {
            time++;

            while (messageAvailable()) {
                Message m = readMessage();
                handleMessage(m);
            }

            StatusMessage status = sense();

            map.updateNeighborhoodTime(status, time);
            map.agents[getAgentId()-1] = map.getAt(status.agentX, status.agentY);
            for (int agentId = 1; agentId <= 4; agentId++) {
                if (agentId != getAgentId()) {
                    sendMessage(agentId, new MapMessage(StatusMessage.AGENT, status.agentX, status.agentY));
                }
            }
            for (StatusMessage.SensorData d : status.sensorInput) {
                // the ... && d.type != AGENT check is redundant while
                // Map implementation returns false when receiving AGENT updates
                if(map.update(d.x, d.y, d.type, time) && d.type != StatusMessage.AGENT) {
                    for (int agentId = 1; agentId <= 4; agentId++) {
                        if (agentId != getAgentId()) {
                            sendMessage(agentId, new MapMessage(d.type, d.x, d.y));
                        }
                    }
                }
            }

            strategy.act(status);
        }
    }

    public Direction getDirection(int fromX, int fromY, int toX, int toY) {
        if (fromX == toX) {
            if (fromY > toY) {
                return Direction.UP;
            } else {
                return Direction.DOWN;
            }
        } else {
            if (fromX > toX) {
                return Direction.LEFT;
            } else {
                return Direction.RIGHT;
            }
        }
    }

    public Direction getDirection(StatusMessage s, Position toPos) {
        return getDirection(s.agentX, s.agentY, toPos.x, toPos.y);
    }

    public StatusMessage goInDirection(Direction direction) throws IOException {
        StatusMessage status = null;
        switch (direction) {
            case RIGHT:
                status = right();
                break;
            case LEFT:
                status = left();
                break;
            case UP:
                status = up();
                break;
            case DOWN:
                status = down();
        }
        return status;
    }

    public StatusMessage randomMoveUntilMoved(StatusMessage origPos, StatusMessage curPos, Random random) throws IOException {
        while (curPos.agentX == origPos.agentX && curPos.agentY == origPos.agentY) {
            curPos = goInDirection(Utils.randomEnum(Direction.class, random));
            try {
                //TODO: maybe remove
                Thread.sleep(random.nextInt(20));
            } catch(InterruptedException ie) {}
        }
        return curPos;
    }

    void handleMessage(Message m) throws Exception {
        if (m instanceof MapMessage) {
            MapMessage M = (MapMessage) m;
            switch (M.type) {
                case StatusMessage.AGENT:
                    //assuming MapMessage with type AGENT is only sent about the sender of a message
                    map.updateNeighborhoodTime(M.x, M.y, time);
                    map.agents[M.getSender()-1] = map.getAt(M.x, M.y);
//                    log(String.format("updated %d", M.getSender()));
                break;
                default:
                    map.update(M, time);
//                    log("I have received " + M);
                    break;
            }
        } else if (m instanceof ClaimMessage) {
            ClaimMessage M = (ClaimMessage) m;
            int agentId = map.updateClaim(M.x, M.y, M.getSender());
            sendMessage(M.getSender(), new ClaimResponseMessage(M.x, M.y, agentId));
        } else if (m instanceof RemoveClaimMessage) {
            RemoveClaimMessage M = (RemoveClaimMessage) m;
            map.removeClaim(M.x, M.y, M.getSender());
        } else if (m instanceof AgentMessage) {
            strategy.handleMessage((AgentMessage) m);
        } else {
            log("I have received " + m);
        }
    }
}
