package student;

import mas.agents.task.mining.StatusMessage;

import java.util.Random;

public class GoToDepotStrategy extends AbstractStrategy {
    final Random random = new Random(43);

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
        status = agent.randomMoveUntilMoved(status, agent.goInDirection(agent.getDirection(status, agent.map.goFromTo(status, agent.depot))), random);
        return status;
    }
}
