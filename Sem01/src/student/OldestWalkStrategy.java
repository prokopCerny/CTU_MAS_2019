package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.util.Random;

public class OldestWalkStrategy extends AbstractStrategy {
    final Random random = new Random(43);

    public OldestWalkStrategy(Agent agent) {
        super(agent);
    }

    @Override
    public boolean canStop() {
        return true;
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        Agent.Direction goTo = agent.getDirection(status, agent.map.goFromTo(status, agent.map.oldestClosest(status)));
        status = agent.randomMoveUntilMoved(status, agent.goInDirection(goTo), random);

//        if (status.isAtGold()) {
//            status = agent.pick();
//        }

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
                        agent.sendMessage(agentID, new MapMessage(data.type, data.x, data.y, agent.getAgentId()));
                    }
                }
            }
        }
        return status;
    }
}
