package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.util.Random;

public class GoToGoldStrategy extends AbstractStrategy {
    final Random random = new Random(43);
    public final Position gold;

    public GoToGoldStrategy(Agent agent, Position gold) {
        super(agent);
        this.gold = gold;
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        if (Utils.manhattanDist(status, gold) > 0) {
            Agent.Direction dir = agent.getDirection(status, agent.map.goFromTo(status, gold));
            status = agent.randomMoveUntilMoved(status, agent.goInDirection(dir), random);
        } else {

        }
        return status;
    }

    @Override
    public void handleMessage(AgentMessage m) throws Exception {
        //TODO
    }
}
