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
    Position depot = null;
    Strategy strategy = new OldestWalkStrategy(this);
    public Map map;
    public long time = 1L;
    boolean hasGold = false;

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


        while (true) {
            time++;

            while (messageAvailable()) {
                Message m = readMessage();
                handleMessage(m);
            }

            StatusMessage status = sense();

            map.updateNeighborhoodTime(status, time);
            for (int agentId = 1; agentId <= 4; agentId++) {
                if (agentId != getAgentId()) {
                    sendMessage(agentId, new MapMessage(StatusMessage.AGENT, status.agentX, status.agentY));
                }
            }
            for (StatusMessage.SensorData d : status.sensorInput) {
                if(map.update(d.x, d.y, d.type, time)) {
                    for (int agentId = 1; agentId <= 4; agentId++) {
                        if (agentId != getAgentId()) {
                            sendMessage(agentId, new MapMessage(d.type, d.x, d.y));
                        }
                    }
                }
            }

            strategy.act(status);



            // REMOVE THIS BEFORE SUBMITTING YOUR SOLUTION TO BRUTE !!
            //   (this is meant just to slow down the execution a bit for demonstration purposes)
//            try {
//                Thread.sleep(2000);
//            } catch(InterruptedException ie) {}
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
        }
        return curPos;
    }

    void handleMessage(Message m) throws Exception {
        if (m instanceof MapMessage) {
            MapMessage M = (MapMessage) m;
            map.update(M, time);
            switch (M.type) {
                case StatusMessage.DEPOT:
                    depot = new Position(M.x, M.y);
                    strategy = new GoToDepotStrategy(this);
                    break;
                    case StatusMessage.AGENT:
                        map.updateNeighborhoodTime(M.x, M.y, time);
                    break;
                default:
                    log("I have received " + M);
                    break;
            }
        } else if (m instanceof ClaimMessage) {
            ClaimMessage M = (ClaimMessage) m;
            int agentId = map.updateClaim(M.x, M.y, M.getSender());
            sendMessage(M.getSender(), new ClaimResponseMessage(M.x, M.y, agentId));
        } else if (m instanceof AgentMessage) {
            strategy.handleMessage((AgentMessage) m);
        } else {
            log("I have received " + m);
        }
    }
}
