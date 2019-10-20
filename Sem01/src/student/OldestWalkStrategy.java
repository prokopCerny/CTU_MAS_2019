package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.util.Comparator;
import java.util.Optional;
import java.util.Random;

public class OldestWalkStrategy extends AbstractStrategy {
    Position currentDestination = null;

    public OldestWalkStrategy(Agent agent) {
        super(agent);
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
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
                                             .filter(n -> {
                                                 if (agent.map.isExplored()) {
                                                     return true;
                                                 } else {
                                                     return Utils.manhattanDist(n.x, n.y, curX, curY) < 3;
                                                 }
                                             })
                                             .min(Comparator.<MapNode>comparingInt(n -> Utils.manhattanDist(n.x, n.y, curX, curY)));
            if (gold.isPresent()) {
                if (agent.map.updateClaim(gold.get().x, gold.get().y, agent.getAgentId()) == agent.getAgentId()) {
                    agent.strategy = new ClaimingStrategy(agent, gold.get().x, gold.get().y);
                    return agent.strategy.act(status);
                }
            }


        }

        if (currentDestination == null || agent.map.getAt(currentDestination).type == StatusMessage.OBSTACLE || Utils.manhattanDist(status, currentDestination) <= 1) {
            currentDestination = agent.map.oldestClosest(status);
            agent.log(String.format("Here (%d, %d), dist: %d", currentDestination.x, currentDestination.y, Utils.manhattanDist(status, currentDestination)));
        }

        Position nextStep = agent.map.goFromTo(status,currentDestination);
        if (nextStep == null) {
            status = agent.randomMoveUntilMoved(status, status, agent.random);
        } else {
            Agent.Direction goTo = agent.getDirection(status, agent.map.goFromTo(status,currentDestination));
            status = agent.randomMoveUntilMoved(status, agent.goInDirection(goTo), agent.random);
        }



        agent.log(String.format("I am now on position [%d,%d] of a %dx%d map.",
                status.agentX, status.agentY, status.width, status.height));
        return status;
    }

    @Override
    public void handleMessage(AgentMessage m) throws Exception {
        //TODO
        if (m instanceof ClaimResponseMessage) {
            ClaimResponseMessage M = (ClaimResponseMessage) m;
            agent.map.updateClaim(M.x, M.y, M.agentId);
        }
//        if (m instanceof HelpMeMessage) {
//            HelpMeMessage M = (HelpMeMessage) m;
//            M.replyWith(new ConfirmationMessage(true));
//            agent.strategy = new GoHelpStrategy(agent, new Position(M.x, M.y), M.getSender(), this);
//        }
    }
}
