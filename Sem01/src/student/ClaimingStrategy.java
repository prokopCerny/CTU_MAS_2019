package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ClaimingStrategy extends AbstractStrategy {
    final Set<Integer> receivedConfirmations;
    final int x;
    final int y;
    public ClaimingStrategy(Agent agent, int x, int y) {
        super(agent);
        try {
            agent.log("Changed to " + this.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.receivedConfirmations = new HashSet<>();
        this.x = x;
        this.y = y;
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        if (agent.map.getAt(x, y).claimedBy == agent.getAgentId()) {
            if (receivedConfirmations.size() == 3) {
                agent.log(String.format("Claimed (%d, %d)!", x, y));
                agent.strategy = new GoToGoldStrategy(agent, new Position(x, y));
//                status = agent.strategy.act(status);
            } else {
                for (int agentId = 1; agentId <= 4; ++agentId) {
                    if (agentId != agent.getAgentId()) {
                        agent.sendMessage(agentId, new ClaimMessage(x, y));
                    }
                }
                Position nextStep = agent.map.goFromTo(status, new Position(x, y));
                if (nextStep != null) {
                    Agent.Direction goTo = agent.getDirection(status, nextStep);
                    status = agent.randomMoveUntilMoved(status, agent.goInDirection(goTo), agent.random);
                }
            }
        } else {
            agent.strategy = new OldestWalkStrategy(agent);
//            status = agent.strategy.act(status);
        }
        return status;
    }

    @Override
    public void visit(ClaimResponseMessage m) {
        if (m.x == x && m.y == y && m.agentId == agent.getAgentId()) {
            receivedConfirmations.add(m.getSender());
        } else {
            agent.map.updateClaim(m.x, m.y, m.agentId);
        }
    }

    @Override
    public void visit(HelpMeMessage m) throws IOException {
        agent.sendMessage(m.getSender(), new WillHelpMessage(m));
        agent.strategy = new WaitForHelpAckStrategy(agent, m.getSender(), m.x, m.y);
    }
}
