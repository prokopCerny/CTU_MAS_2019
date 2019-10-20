package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

public class WaitForHelpAckStrategy extends AbstractStrategy {
    final int helpWho;
    final int x;
    final int y;
    final Position meanwhileGoTo;

    public WaitForHelpAckStrategy(Agent agent, int helpWho, int x, int y, Position meanwhileGoTo) {
        super(agent);
        this.helpWho = helpWho;
        this.x = x;
        this.y = y;
        this.meanwhileGoTo = meanwhileGoTo;
        try {
            agent.log("Changed to " + this.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        Position nextStep = agent.map.goFromTo(status, meanwhileGoTo);
        if (nextStep == null) {
            if (Utils.manhattanDist(status, meanwhileGoTo) > 0) {
                status = agent.randomMoveUntilMoved(status, status, agent.random);
            }
        } else {
            Agent.Direction goTo = agent.getDirection(status, nextStep);
            status = agent.randomMoveUntilMoved(status, agent.goInDirection(goTo), agent.random);
        }
        return status;
    }

    @Override
    public void visit(HelpAcceptedMessage m) {
        if (m.getSender() != helpWho) {
            throw new RuntimeException("Help accepted by unexpected agent!");
        } else {
            agent.strategy = new GoHelpStrategy(agent, helpWho, x, y);
        }
    }

    @Override
    public void visit(HelpRefusedMessage m) {
        if (m.getSender() != helpWho) {
            throw new RuntimeException("Help refused by unexpected agent!");
        } else {
            agent.strategy = new OldestWalkStrategy(agent);
        }
    }
}
