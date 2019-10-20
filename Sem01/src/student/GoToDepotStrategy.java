package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;

public class GoToDepotStrategy extends AbstractStrategy {
//    Position currentDestination = null;

    public GoToDepotStrategy(Agent agent) {
        super(agent);
        try {
            agent.log("Changed to " + this.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        if (agent.hasGold) {
            Optional<Position> depot = agent.map.getDepot();
            if (depot.isPresent()) {
                Position nextStep = agent.map.goFromTo(status, depot.get());
                if (nextStep == null) {
                    if (Utils.manhattanDist(status, depot.get()) == 0) {
                        status = agent.drop();
                        agent.hasGold = false;
                    } else {
                        status = agent.randomMoveUntilMoved(status, status, agent.random);
                    }
                } else {
//                    agent.log("Going to depo!");
                    status = agent.randomMoveUntilMoved(status, agent.goInDirection(agent.getDirection(status, nextStep)), agent.random);
                }
            } else {
//                if (currentDestination == null || agent.map.getAt(currentDestination).type == StatusMessage.OBSTACLE || Utils.manhattanDist(status, currentDestination) <= 1) {
//                    currentDestination = currentDestination = agent.map.oldestClosest(status);
//                }
                Position nextStep = agent.map.goFromTo(status, agent.map.oldestClosest(status));
                if (nextStep != null) {
                    status = agent.randomMoveUntilMoved(status, agent.goInDirection(agent.getDirection(status, nextStep)), agent.random);
                } else {
                    status = agent.randomMoveUntilMoved(status, status, agent.random);
                }
            }
        } else {
            agent.strategy = new OldestWalkStrategy(agent);
        }
        return status;
    }

    @Override
    public void visit(HelpMeMessage m) throws IOException {
        Optional<Position> depot = agent.map.getDepot();
        if (depot.isPresent()) {
            Position depo = depot.get();
            Position myPos = agent.map.agents[agent.getAgentId()-1].getPosition();
            Position askerPos = agent.map.agents[m.getSender()-1].getPosition();
            Position askerDest = new Position(m.x, m.y);
            if (Utils.manhattanDist(depo, myPos) > Utils.manhattanDist(askerPos, askerDest)) {
                agent.sendMessage(m.getSender(), new WontHelpMessage(m));
            } else {
                agent.strategy = new WaitForHelpAckStrategy(agent, m.getSender(), m.x, m.y);
                agent.sendMessage(m.getSender(), new WillHelpMessage(m));
            }
        } else {
            agent.strategy = new WaitForHelpAckStrategy(agent, m.getSender(), m.x, m.y);
            agent.sendMessage(m.getSender(), new WillHelpMessage(m));
        }
    }
}
