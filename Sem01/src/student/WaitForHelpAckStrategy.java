package student;

import mas.agents.task.mining.StatusMessage;

public class WaitForHelpAckStrategy extends AbstractStrategy {
    final int helpWho;
    final int x;
    final int y;

    public WaitForHelpAckStrategy(Agent agent, int helpWho, int x, int y) {
        super(agent);
        this.helpWho = helpWho;
        this.x = x;
        this.y = y;
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
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
