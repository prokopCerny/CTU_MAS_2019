package student;

import mas.agents.task.mining.Position;
import mas.agents.task.mining.StatusMessage;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WaitHelpReplyStrategy extends AbstractStrategy {
    final Set<Integer> receivedWillHelps;
    final Set<Integer> receivedWontHelps;
    final int x;
    final int y;
    public WaitHelpReplyStrategy(Agent agent, int x, int y) {
        super(agent);
        receivedWontHelps = new HashSet<>();
        receivedWillHelps = new HashSet<>();
        this.x = x;
        this.y = y;
        try {
            agent.log("Changed to " + this.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public StatusMessage act(StatusMessage status) throws Exception {
        if (! status.isAtGold()) {
            throw new RuntimeException("Waiting for reply while not standing at gold?!");
//            agent.strategy = new OldestWalkStrategy(agent);
        }
        if (receivedWontHelps.size() == 3) {
            agent.strategy = new OldestWalkStrategy(agent);
        } else if (receivedWontHelps.size() + receivedWillHelps.size() == 3) {
            List<Integer> sortedNearest = receivedWillHelps.stream()
                                                  .sorted(Comparator.comparingInt(a -> Utils.manhattanDist(status, agent.map.agents[a-1].getPosition())))
                                                  .collect(Collectors.toList());
            agent.sendMessage(sortedNearest.get(0), new HelpAcceptedMessage());
            for (int i = 1; i < sortedNearest.size(); i++) {
                agent.sendMessage(sortedNearest.get(i), new HelpRefusedMessage());
            }
            agent.strategy = new SittingAtGoldStrategy(agent, sortedNearest.get(0), x, y);
        }
        return status;
    }

    @Override
    public void visit(HelpMeMessage m) throws IOException {
        if (m.getSender() < agent.getAgentId()) {
            agent.strategy = new WaitForHelpAckStrategy(agent, m.getSender(), m.x, m.y, new Position(m.x, m.y));
            for (int waitingAgent : receivedWillHelps) {
                agent.sendMessage(waitingAgent, new HelpRefusedMessage());
            }
            agent.sendMessage(m.getSender(), new WillHelpMessage(m));
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
            receivedWillHelps.add(m.getSender());
//            agent.sendMessage(m.getSender(), new HelpAcceptedMessage());
//            agent.strategy = new SittingAtGoldStrategy(agent, m.getSender(), x, y);
        } else {
            agent.sendMessage(m.getSender(), new HelpRefusedMessage());
            throw new RuntimeException("Received will help for unexpected location");
        }
    }
}
