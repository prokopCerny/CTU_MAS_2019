package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.util.Random;

public class GoHelpStrategy extends AbstractStrategy {
    final Strategy previousStrategy;
    final Position helpWhere;
    final int helpWho;
    final Random random = new Random(43);

    public GoHelpStrategy(Agent agent, Position helpWhere, int helpWho, Strategy previousStrategy) {
        super(agent);
        this.previousStrategy = previousStrategy;
        this.helpWhere = helpWhere;
        this.helpWho = helpWho;
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        if (Utils.manhattanDist(status, helpWhere) == 1) {
            //TODO
        } else {
            Position nextStep = agent.map.goFromTo(status, helpWhere, 1);
            status = agent.randomMoveUntilMoved(status, agent.goInDirection(agent.getDirection(status, nextStep)), random);
        }
        return status;
    }

    @Override
    public void handleMessage(AgentMessage m) throws Exception {
        if (m instanceof HelpMeMessage) {
            ((HelpMeMessage) m).replyWith(new ConfirmationMessage(false));
        }
    }
}
