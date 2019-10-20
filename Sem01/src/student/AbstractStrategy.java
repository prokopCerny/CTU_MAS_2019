package student;

import java.io.IOException;

public abstract class AbstractStrategy implements Strategy, AgentMessageVisitor {
    final Agent agent;
    protected boolean canChange = true;

    public AbstractStrategy(Agent agent) {
        this.agent = agent;
    }

    @Override
    public final boolean canChange() {
        return canChange;
    }

    @Override
    public final void handleMessage(AgentMessage m) throws Exception {
        m.accept(this);
    }

    @Override
    public void visit(ClaimResponseMessage m) {
        agent.map.updateClaim(m.x, m.y, m.agentId);
    }

    @Override
    public void visit(HelpMeMessage m) throws IOException {
        agent.sendMessage(m.getSender(), new WontHelpMessage(m));
    }

    @Override
    public void visit(WontHelpMessage m) {

    }

    @Override
    public void visit(WillHelpMessage m) throws IOException {
        agent.sendMessage(m.getSender(), new HelpRefusedMessage());
    }

    @Override
    public void visit(HelpAcceptedMessage m) {
        throw new RuntimeException("Unexpected help acceptance!");
    }

    @Override
    public void visit(HelpRefusedMessage m) {
        throw new RuntimeException("Unexpected help refusal!");
    }

    @Override
    public void visit(InPositionMessage m) {
        throw new RuntimeException("Unexpected in position message!");
    }
}
