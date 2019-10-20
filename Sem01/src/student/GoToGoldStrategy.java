package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.util.Random;

public class GoToGoldStrategy extends AbstractStrategy {
    public final Position gold;

    public GoToGoldStrategy(Agent agent, Position gold) {
        super(agent);
        this.gold = gold;
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

        }
        return status;
    }

    @Override
    public void handleMessage(AgentMessage m) throws Exception {
        //TODO
        if (m instanceof ClaimResponseMessage) {
            ClaimResponseMessage M = (ClaimResponseMessage) m;
            agent.map.updateClaim(M.x, M.y, M.agentId);
        }
    }
}
