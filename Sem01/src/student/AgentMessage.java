package student;

import mas.agents.Message;

import java.io.IOException;

public abstract class AgentMessage extends Message {
    public AgentMessage() {
        super();
    }

    public abstract void accept(AgentMessageVisitor visitor) throws IOException;
}
