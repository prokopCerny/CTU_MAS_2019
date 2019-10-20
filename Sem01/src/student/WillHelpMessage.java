package student;

import java.io.IOException;

public class WillHelpMessage extends AgentMessage {
    final int x;
    final int y;

    public WillHelpMessage(HelpMeMessage m) {
        super();
        this.x = m.x;
        this.y = m.y;
    }

    @Override
    public void accept(AgentMessageVisitor visitor) throws IOException {
        visitor.visit(this);
    }
}
