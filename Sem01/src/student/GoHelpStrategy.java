package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

public class GoHelpStrategy extends AbstractStrategy {
    final int helpWho;
    final Position helpWhere;

    public GoHelpStrategy(Agent agent, int helpWho, int x, int y) {
        super(agent);
        try {
            agent.log("Changed to" + this.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            if (agent.map.agents[helpWho-1].x == helpWhere.x && agent.map.agents[helpWho-1].y == helpWhere.y) {
                agent.strategy = new InPositionStrategy(agent, helpWho);
                agent.sendMessage(helpWho, new InPositionMessage());
            } else {
                agent.strategy = new OldestWalkStrategy(agent);
                agent.log(String.format("Expected %d to be at (%d, %d), but isn't", helpWho, helpWhere.x, helpWhere.y));
            }
        }
        return status;
    }
}
