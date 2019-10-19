package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.util.HashSet;
import java.util.Set;

public class ClaimingStrategy extends AbstractStrategy {
    Set<Integer> receivedConfirmations;
    final int x;
    final int y;
    public ClaimingStrategy(Agent agent, int x, int y) {
        super(agent);
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
                status = agent.strategy.act(status);
            } else {
                for (int agentId = 1; agentId <= 4; ++agentId) {
                    if (agentId != agent.getAgentId()) {
                        agent.sendMessage(agentId, new ClaimMessage(x, y));
                    }
                }
            }
        } else {
            agent.strategy = new OldestWalkStrategy(agent);
            status = agent.strategy.act(status);
        }
        return status;
    }

    @Override
    public void handleMessage(AgentMessage m) throws Exception {
        //TODO
        if (m instanceof ClaimResponseMessage) {
            ClaimResponseMessage M = (ClaimResponseMessage) m;
            if (M.x == x && M.y == y && M.agentId == agent.getAgentId()) {
                receivedConfirmations.add(M.getSender());
            } else {
                agent.map.updateClaim(M.x, M.y, M.agentId);
            }
        }

    }
}
