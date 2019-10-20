package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.io.IOException;
import java.util.Random;

public class RandomWalkStrategy extends AbstractStrategy {

    public RandomWalkStrategy(Agent agent) {
        super(agent);
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        switch (agent.random.nextInt(4)) {
            case 0:
                status = agent.left();
                break;
            case 1:
                status = agent.right();
                break;
            case 2:
                status = agent.up();
                break;
            default:
                status = agent.down();
                break;
        }
        if (status.isAtGold()) {
            status = agent.pick();
        }
        agent.log(String.format("I am now on position [%d,%d] of a %dx%d map.",
                status.agentX, status.agentY, status.width, status.height));
        for(StatusMessage.SensorData data : status.sensorInput) {
            agent.log(String.format("I see %s at [%d,%d]", agent.types[data.type], data.x, data.y));
            if (data.type == StatusMessage.DEPOT) {
                agent.depot = new Position(data.x, data.y);
                agent.strategy = new GoToDepotStrategy(agent);
                for (int agentID = 1; agentID <= 4; agentID++) {
                    if (agentID != agent.getAgentId()) {
                        agent.log(String.format("Sending depo to %d", agentID));
                        agent.sendMessage(agentID, new MapMessage(data.type, data.x, data.y));
                    }
                }
            }
        }
        return status;
    }

    @Override
    public void handleMessage(AgentMessage m) throws Exception {
        if (m instanceof ClaimResponseMessage) {
            ClaimResponseMessage M = (ClaimResponseMessage) m;
            agent.map.updateClaim(M.x, M.y, M.agentId);
        }

    }
}
