package student;

public class ClaimResponseMessage extends AgentMessage {
    final int x;
    final int y;
    final int agentId;

    public ClaimResponseMessage(int x, int y, int agentId) {
        super();
        this.x = x;
        this.y = y;
        this.agentId = agentId;
    }

    @Override
    public void accept(AgentMessageVisitor visitor) {
        visitor.visit(this);
    }
}
