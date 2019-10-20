package student;

import mas.agents.task.mining.StatusMessage;

public class SittingAtGoldStrategy extends AbstractStrategy {
    final int helperAgent;
    final int x;
    final int y;
    boolean helperReady = false;

    public SittingAtGoldStrategy(Agent agent, int helperAgent, int x, int y) {
        super(agent);
        this.helperAgent = helperAgent;
        this.x = x;
        this.y = y;
        try {
            agent.log("Changed to" + this.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        if (! status.isAtGold()) {
            throw new RuntimeException("Agent " + agent.getAgentId() + " Sitting at gold while not standing at gold?!");
        }
        if (helperReady) {
            status = agent.pick();
            agent.map.removeClaim(x, y, agent.getAgentId());
            agent.hasGold = true;
            for (int agentId : agent.map.orderNearestOtherAgents(x, y)) {
                agent.sendMessage(agentId, new RemoveClaimMessage(x, y));
            }
            agent.strategy = new GoToDepotStrategy(agent);
        }
        return status;
    }

    @Override
    public void visit(InPositionMessage m) {
        if (m.getSender() == helperAgent) {
            helperReady = true;
        } else  {
            throw new RuntimeException(String.format("Unexpected InPosition message, expected from %d, got from %d", helperAgent, m.getSender()));
        }
    }
}
