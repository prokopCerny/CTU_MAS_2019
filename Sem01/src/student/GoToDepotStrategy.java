package student;

import mas.agents.task.mining.StatusMessage;

public class GoToDepotStrategy extends AbstractStrategy {

    public GoToDepotStrategy(Agent agent) {
        super(agent);
    }

    @Override
    public boolean canStop() {
        return false;
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        agent.log("Going to depo!");
        if (Math.abs(status.agentX - agent.depot.x) > Math.abs(status.agentY - agent.depot.y)) {
            if (status.agentX > agent.depot.x) {
                status = agent.left();
            } else if (status.agentX < agent.depot.x){
                status = agent.right();
            }
        } else {
            if (status.agentY > agent.depot.y) {
                status = agent.up();
            } else if (status.agentY < agent.depot.y){
                status = agent.down();
            }
        }
        return status;
    }
}
