package student;

public class HelpAcceptedMessage extends AgentMessage {
    public HelpAcceptedMessage() {
    }

    @Override
    public void accept(AgentMessageVisitor visitor) {
        visitor.visit(this);
    }
}
