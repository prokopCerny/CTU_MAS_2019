package student;

import mas.agents.Message;

public class RemoveClaimMessage extends Message {
    final int x;
    final int y;

    public RemoveClaimMessage(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
