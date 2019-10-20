package student;

import mas.agents.task.mining.StatusMessage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class WaitHelpReplyStrategy extends AbstractStrategy {
    final Set<Integer> receivedWontHelps;
    final int x;
    final int y;
    public WaitHelpReplyStrategy(Agent agent, int x, int y) {
        super(agent);
        receivedWontHelps = new HashSet<>();
        this.x = x;
        this.y = y;
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        if (! status.isAtGold()) {
            throw new RuntimeException("Waiting for reply while not standing at gold?!");
        }
        if (receivedWontHelps.size() == 3) {
            agent.strategy = new OldestWalkStrategy(agent);
        }
        return status;
    }

    @Override
    public void visit(HelpMeMessage m) throws IOException {
        if (m.getSender() < agent.getAgentId()) {
            agent.sendMessage(m.getSender(), new WillHelpMessage(m));
            agent.strategy = new WaitForHelpAckStrategy(agent, m.getSender(), m.x, m.y);
        } else {
            agent.sendMessage(m.getSender(), new WontHelpMessage(m));
        }
    }

    @Override
    public void visit(WontHelpMessage m) {
        if (m.x == x && m.y == y) {
            receivedWontHelps.add(m.getSender());
        } else {
            throw new RuntimeException("Received wont help for unexpected location");
        }
    }

    @Override
    public void visit(WillHelpMessage m) throws IOException {
        if (m.x == x && m.y == y) {
            agent.sendMessage(m.getSender(), new HelpAcceptedMessage());
            agent.strategy = new SittingAtGoldStrategy(agent, m.getSender(), x, y);
        } else {
            agent.sendMessage(m.getSender(), new HelpRefusedMessage());
            throw new RuntimeException("Received wont help for unexpected location");
        }
    }
}
