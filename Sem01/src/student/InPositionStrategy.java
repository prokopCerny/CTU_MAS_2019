package student;

import mas.agents.task.mining.StatusMessage;

public class InPositionStrategy extends AbstractStrategy {
    final int helpingWho;
    public InPositionStrategy(Agent agent, int helpWho) {
        super(agent);
        this.helpingWho = helpWho;
        try {
            agent.log("Changed to " + this.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        if (agent.map.agentClaims[helpingWho-1] == null) {
            agent.strategy = new OldestWalkStrategy(agent);
        }
        return status;
    }


}
