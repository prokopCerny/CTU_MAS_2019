package student;

public class ConfirmationMessage extends AgentMessage {
    public final boolean confirmation;

    public ConfirmationMessage(boolean confirmation) {
        super();
        this.confirmation = confirmation;
    }
}
