package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class GoToGoldStrategy extends AbstractStrategy {
    public final Position gold;

    public GoToGoldStrategy(Agent agent, Position gold) {
        super(agent);
        this.gold = gold;
        try {
            agent.log("Changed to" + this.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        if (Utils.manhattanDist(status, gold) > 0) {
            Position nextStep = agent.map.goFromTo(status, gold);
            if (nextStep == null) {
                status = agent.randomMoveUntilMoved(status, status, agent.random);
//                throw new NullPointerException("WHAAT");
            } else {
                Agent.Direction dir = agent.getDirection(status, nextStep);
                status = agent.randomMoveUntilMoved(status, agent.goInDirection(dir), agent.random);
            }
        } else {
            List<Integer> nearestAgents = agent.map.orderNearestOtherAgents(status.agentX, status.agentY);
            agent.strategy = new WaitHelpReplyStrategy(agent, status.agentX, status.agentY);
            for (int agentId : nearestAgents) {
                //TODO
                agent.sendMessage(agentId, new HelpMeMessage(status.agentX, status.agentY));
                Thread.sleep(10);
            }

        }
        return status;
    }

    @Override
    public void visit(HelpMeMessage m) throws IOException {
        agent.sendMessage(m.getSender(), new WillHelpMessage(m));
        agent.strategy = new WaitForHelpAckStrategy(agent, m.getSender(), m.x, m.y);
    }
}
