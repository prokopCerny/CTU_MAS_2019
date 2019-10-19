package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.util.Comparator;
import java.util.Optional;
import java.util.Random;

public class OldestWalkStrategy extends AbstractStrategy {
    final Random random = new Random(43);

    public OldestWalkStrategy(Agent agent) {
        super(agent);
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
//        if (status.isAtGold()) {
//            status = agent.pick();
//        }
        if (!agent.hasGold) {
            if (agent.map.agentClaims[agent.getAgentId()-1] != null) {
                MapNode n = agent.map.agentClaims[agent.getAgentId()-1];
                agent.strategy = new ClaimingStrategy(agent, n.x, n.y);
                return agent.strategy.act(status);
            }
            final int curX = status.agentX;
            final int curY = status.agentY;
            Optional<MapNode> gold = agent.map.map.stream()
                                             .filter(n -> n.type == StatusMessage.GOLD)
                                             .filter(n -> n.claimedBy == -1)
                                             .min(Comparator.<MapNode>comparingInt(n -> Utils.manhattanDist(n.x, n.y, curX, curY)));
            if (gold.isPresent()) {
                if (agent.map.updateClaim(gold.get().x, gold.get().y, agent.getAgentId()) == agent.getAgentId()) {
                    agent.strategy = new ClaimingStrategy(agent, gold.get().x, gold.get().y);
                    return agent.strategy.act(status);
                }
            }


        }

        Agent.Direction goTo = agent.getDirection(status, agent.map.goFromTo(status, agent.map.oldestClosest(status)));
        status = agent.randomMoveUntilMoved(status, agent.goInDirection(goTo), random);

        agent.log(String.format("I am now on position [%d,%d] of a %dx%d map.",
                status.agentX, status.agentY, status.width, status.height));
        for(StatusMessage.SensorData data : status.sensorInput) {
            agent.log(String.format("I see %s at [%d,%d]", Agent.types[data.type], data.x, data.y));
            if (data.type == StatusMessage.DEPOT) {
                agent.depot = new Position(data.x, data.y);
                agent.strategy = new GoToDepotStrategy(agent);
            } else if (data.type == StatusMessage.GOLD && !agent.hasGold) {


            }
        }
        return status;
    }

    @Override
    public void handleMessage(AgentMessage m) throws Exception {
        //TODO
//        if (m instanceof HelpMeMessage) {
//            HelpMeMessage M = (HelpMeMessage) m;
//            M.replyWith(new ConfirmationMessage(true));
//            agent.strategy = new GoHelpStrategy(agent, new Position(M.x, M.y), M.getSender(), this);
//        }
    }
}
