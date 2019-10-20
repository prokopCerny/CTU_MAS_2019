package student;

public class HelpRefusedMessage extends AgentMessage {
    public HelpRefusedMessage() {
    }

    @Override
    public void accept(AgentMessageVisitor visitor) {
        visitor.visit(this);
    }
}
