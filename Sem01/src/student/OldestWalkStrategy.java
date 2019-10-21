package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;

public class OldestWalkStrategy extends AbstractStrategy {
    Position currentDestination = null;

    public OldestWalkStrategy(Agent agent) {
        super(agent);
        try {
            agent.log("Changed to " + this.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        } else {
            agent.strategy = new GoToDepotStrategy(agent);
        }

        if (currentDestination == null
                    || agent.map.getAt(currentDestination).type == StatusMessage.OBSTACLE
                    || Utils.manhattanDist(status, currentDestination) < 1
                    || agent.map.getAt(currentDestination).lastSeen > agent.map.oldestAge()) {
            currentDestination = agent.map.oldestClosest(status);
//            agent.log(String.format("Here (%d, %d), dist: %d", currentDestination.x, currentDestination.y, Utils.manhattanDist(status, currentDestination)));
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
    public void visit(HelpMeMessage m) throws IOException {
        agent.sendMessage(m.getSender(), new WillHelpMessage(m));
        if (currentDestination == null) {
            currentDestination = agent.map.oldestClosest(agent.map.agents[agent.getAgentId()-1].getPosition());
        }
        agent.strategy = new WaitForHelpAckStrategy(agent, m.getSender(), m.x, m.y, currentDestination);
    }
}
