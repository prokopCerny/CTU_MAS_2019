package student;

public class WontHelpMessage extends AgentMessage {
    final int x;
    final int y;

    public WontHelpMessage(HelpMeMessage m) {
        super();
        this.x = m.x;
        this.y = m.y;
    }

    @Override
    public void accept(AgentMessageVisitor visitor) {
        visitor.visit(this);
    }
}
