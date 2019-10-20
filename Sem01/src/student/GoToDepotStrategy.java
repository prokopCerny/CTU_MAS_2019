package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.util.Optional;
import java.util.Random;

public class GoToDepotStrategy extends AbstractStrategy {
    Strategy oldestWalk = null;

    public GoToDepotStrategy(Agent agent) {
        super(agent);
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        if (agent.hasGold) {
            Optional<Position> depot = agent.map.getDepot();
            if (depot.isPresent()) {
                Position nextStep = agent.map.goFromTo(status, depot.get());
                if (nextStep == null) {
                    status = agent.drop();
                    agent.hasGold = false;
                } else {
                    agent.log("Going to depo!");
                    status = agent.randomMoveUntilMoved(status, agent.goInDirection(agent.getDirection(status, nextStep)), agent.random);
                }
            } else {
                status = getOldestWalk().act(status);
            }
        } else {
            agent.strategy = new OldestWalkStrategy(agent);
        }
        return status;
    }

    @Override
    public void handleMessage(AgentMessage m) throws Exception {
        //TODO
//        if (m instanceof HelpMeMessage) {
//            HelpMeMessage M = (HelpMeMessage) m;
//            M.replyWith(new ConfirmationMessage(true));
//            agent.strategy = new GoHelpStrategy(agent, new Position(M.x, M.y), M.getSender(), this);
//        }
    }

    private Strategy getOldestWalk() {
        if (oldestWalk == null) {
            oldestWalk = new OldestWalkStrategy(agent);
        }
        return oldestWalk;
    }
}
