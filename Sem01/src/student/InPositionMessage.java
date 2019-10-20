package student;

import mas.agents.Message;

public class InPositionMessage extends AgentMessage {
    @Override
    public void accept(AgentMessageVisitor visitor) {
        visitor.visit(this);
    }
}
