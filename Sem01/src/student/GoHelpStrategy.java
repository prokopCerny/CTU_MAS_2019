package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

public class GoHelpStrategy extends AbstractStrategy {
    final int helpWho;
    final Position helpWhere;

    public GoHelpStrategy(Agent agent, int helpWho, int x, int y) {
        super(agent);
        this.helpWho = helpWho;
        this.helpWhere = new Position(x, y);
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        if (Utils.manhattanDist(status, helpWhere) > 1) {
            Position nextStep = agent.map.goFromTo(status, helpWhere, 1);
            if (nextStep != null) {
                status = agent.randomMoveUntilMoved(status, agent.goInDirection(agent.getDirection(status, nextStep)), agent.random);
            } else {
                status = agent.randomMoveUntilMoved(status, status, agent.random);
            }
        } else {
            agent.sendMessage(helpWho, new InPositionMessage());
            agent.strategy = new InPositionStrategy(agent, helpWho);
        }
        return status;
    }
}
