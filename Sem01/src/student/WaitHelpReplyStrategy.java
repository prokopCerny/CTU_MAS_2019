package student;

import mas.agents.task.mining.StatusMessage;

public class WaitHelpReplyStrategy extends AbstractStrategy {
    final int x;
    final int y;
    public WaitHelpReplyStrategy(Agent agent, int x, int y) {
        super(agent);
        this.x = x;
        this.y = y;
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        if (! status.isAtGold()) {
            throw new RuntimeException("Waiting for reply while not standing at gold?!");
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
