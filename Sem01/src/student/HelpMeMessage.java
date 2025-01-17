package student;

import java.io.IOException;

public class HelpMeMessage extends AgentMessage {
    public final int x;
    public final int y;

    public HelpMeMessage(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    @Override
    public void accept(AgentMessageVisitor visitor) throws IOException {
        visitor.visit(this);
    }
}
