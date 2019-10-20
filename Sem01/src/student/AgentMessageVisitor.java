package student;

import java.io.IOException;

public interface AgentMessageVisitor {
    void visit(ClaimResponseMessage m);
    void visit(HelpMeMessage m) throws IOException;
    void visit(WontHelpMessage m);
    void visit(WillHelpMessage m) throws IOException;
    void visit(HelpAcceptedMessage m);
    void visit(HelpRefusedMessage m);
    void visit(InPositionMessage m);
}
